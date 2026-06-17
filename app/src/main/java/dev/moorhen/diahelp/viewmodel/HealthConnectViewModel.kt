package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
}

class HealthConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val sugarRepo = SugarRepository(application)
    private val insulinRepo = InsulinRepository(application)
    private val session = SessionManager(application)

    val hcState = MutableLiveData<HcState>(HcState.Idle)

    /** Проверяет доступность HC и разрешения, возвращает состояние через LiveData. */
    fun checkStatus() {
        val ctx = getApplication<Application>()
        val client = HealthConnectManager.getClientOrNull(ctx)
        if (client == null) {
            hcState.value = HcState.NotAvailable
            return
        }
        viewModelScope.launch {
            hcState.value = if (HealthConnectManager.hasAllPermissions(client))
                HcState.Idle
            else
                HcState.NeedsPermissions
        }
    }

    /**
     * Экспортирует все записи сахара и инсулина текущего пользователя
     * в Health Connect.
     */
    fun exportToHealthConnect() {
        val ctx = getApplication<Application>()
        val client = HealthConnectManager.getClientOrNull(ctx) ?: run {
            hcState.value = HcState.NotAvailable; return
        }
        val userId = session.getUserId()
        if (userId == -1) { hcState.value = HcState.Error("Пользователь не авторизован"); return }

        hcState.value = HcState.Loading
        viewModelScope.launch {
            try {
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

    /**
     * Импортирует записи глюкозы и инсулина из Health Connect за последние 30 дней
     * и сохраняет их в локальную БД DiaHelp.
     */
    fun importFromHealthConnect() {
        val ctx = getApplication<Application>()
        val client = HealthConnectManager.getClientOrNull(ctx) ?: run {
            hcState.value = HcState.NotAvailable; return
        }
        val userId = session.getUserId()
        if (userId == -1) { hcState.value = HcState.Error("Пользователь не авторизован"); return }

        hcState.value = HcState.Loading
        viewModelScope.launch {
            try {
                if (!HealthConnectManager.hasAllPermissions(client)) {
                    hcState.value = HcState.NeedsPermissions; return@launch
                }

                val sugarRecords = HealthConnectManager.readBloodGlucose(client, userId)
                val insulinRecords = HealthConnectManager.readInsulinDelivery(client, userId)

                sugarRecords.forEach { sugarRepo.insert(it) }
                insulinRecords.forEach { insulinRepo.insert(it) }

                hcState.value = HcState.Success(
                    "Импортировано: ${sugarRecords.size} замеров сахара, ${insulinRecords.size} доз инсулина"
                )
            } catch (e: Exception) {
                hcState.value = HcState.Error("Ошибка импорта: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() { hcState.value = HcState.Idle }
}
