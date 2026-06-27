package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.moorhen.diahelp.data.db.AppDatabase
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.utils.HealthConnectManager
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.launch

/** Состояние UI для блока Health Connect. */
sealed class HcState {
    object Idle : HcState()
    object Loading : HcState()
    data class Success(val message: String) : HcState()
    data class Error(val message: String) : HcState()
    object NotAvailable : HcState()
    object NeedsPermissions : HcState()
    /** Сессия устарела — пользователь удалён из БД, нужно войти заново */
    object SessionExpired : HcState()
}

class HealthConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val sugarRepo = SugarRepository(application)
    private val insulinRepo = InsulinRepository(application)
    private val session = SessionManager(application)
    private val userDao = AppDatabase.getDatabase(application).userDao()

    val hcState = MutableLiveData<HcState>(HcState.Idle)

    /**
     * Проверяет доступность HC и разрешения.
     * Также проверяет что текущий userId реально существует в БД —
     * защита от ситуации когда БД была пересоздана (fallbackToDestructiveMigration),
     * но SharedPreferences сохранили старый userId.
     */
    fun checkStatus() {
        val ctx = getApplication<Application>()
        val client = HealthConnectManager.getClientOrNull(ctx)
        if (client == null) {
            hcState.value = HcState.NotAvailable
            return
        }
        viewModelScope.launch {
            // Сначала проверяем что сессия валидна
            if (!isSessionValid()) {
                hcState.value = HcState.SessionExpired
                return@launch
            }
            hcState.value = if (HealthConnectManager.hasAllPermissions(client))
                HcState.Idle
            else
                HcState.NeedsPermissions
        }
    }

    /**
     * Проверяет что userId из SharedPreferences существует в БД.
     * Если нет — очищает сессию чтобы пользователь мог войти заново.
     */
    private suspend fun isSessionValid(): Boolean {
        val userId = session.getUserId()
        if (userId == -1) return false
        val user = userDao.getUserByIdSuspend(userId)
        if (user == null) {
            // Пользователь не найден в БД — сессия устарела, очищаем
            session.logout()
            return false
        }
        return true
    }

    fun exportToHealthConnect() {
        val ctx = getApplication<Application>()
        val client = HealthConnectManager.getClientOrNull(ctx) ?: run {
            hcState.value = HcState.NotAvailable; return
        }
        val userId = session.getUserId()
        if (userId == -1) { hcState.value = HcState.SessionExpired; return }

        hcState.value = HcState.Loading
        viewModelScope.launch {
            try {
                if (!isSessionValid()) { hcState.value = HcState.SessionExpired; return@launch }
                if (!HealthConnectManager.hasAllPermissions(client)) {
                    hcState.value = HcState.NeedsPermissions; return@launch
                }
                val sugar = sugarRepo.getAllSugarNotesByUserId(userId)
                val insulin = insulinRepo.getAllInsulinNotesByUserId(userId)

                val sugarCount = HealthConnectManager.writeSugarNotes(client, sugar)
                val insulinCount = HealthConnectManager.writeInsulinNotes(client, insulin)

                hcState.value = HcState.Success(
                    "Экспортировано: $sugarCount замеров сахара, $insulinCount доз инсулина"
                )
            } catch (e: Exception) {
                hcState.value = HcState.Error("Ошибка экспорта: ${e.localizedMessage}")
            }
        }
    }

    fun importFromHealthConnect() {
        val ctx = getApplication<Application>()
        val client = HealthConnectManager.getClientOrNull(ctx) ?: run {
            hcState.value = HcState.NotAvailable; return
        }
        val userId = session.getUserId()
        if (userId == -1) { hcState.value = HcState.SessionExpired; return }

        hcState.value = HcState.Loading
        viewModelScope.launch {
            try {
                // Критически важно: проверяем что userId существует в БД
                // прежде чем пытаться вставить записи с FOREIGN KEY на users.id
                if (!isSessionValid()) {
                    hcState.value = HcState.SessionExpired
                    return@launch
                }

                if (!HealthConnectManager.hasAllPermissions(client)) {
                    hcState.value = HcState.NeedsPermissions; return@launch
                }

                val sugarRecords = HealthConnectManager.readBloodGlucose(client, userId)
                val insulinRecords = HealthConnectManager.readInsulinDelivery(client, userId)

                val addedSugarCount = sugarRecords.count { sugarRepo.insertIfNotExists(it) }
                insulinRecords.forEach { insulinRepo.insert(it) }

                hcState.value = HcState.Success(
                    "Импортировано: $addedSugarCount новых замеров сахара " +
                            "(из ${sugarRecords.size} найденных в HC)"
                )
            } catch (e: Exception) {
                hcState.value = HcState.Error("Ошибка импорта: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() { hcState.value = HcState.Idle }
}
