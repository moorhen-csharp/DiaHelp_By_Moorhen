package dev.moorhen.diahelp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.utils.NotificationScheduler
import dev.moorhen.diahelp.utils.ReportManager
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val sugarRepository = SugarRepository(application)
    private val insulinRepository = InsulinRepository(application)
    private val reportManager = ReportManager(application)

    private val _logout = MutableLiveData<Boolean>()
    val logout: LiveData<Boolean> get() = _logout

    // Файл готового отчёта (для отправки через Intent.ACTION_SEND)
    private val _exportedFile = MutableLiveData<File?>()
    val exportedFile: LiveData<File?> get() = _exportedFile

    private val _exportError = MutableLiveData<String?>()
    val exportError: LiveData<String?> get() = _exportError

    fun onLogoutClicked() {
        sessionManager.logout()
        _logout.value = true
    }

    fun getUserName(): String = sessionManager.getUsername() ?: "Неизвестно"
    fun getUserEmail(): String = sessionManager.getEmail() ?: "Нет данных"
    fun getUserCoeffInsulin(): Double = sessionManager.getCoeffInsulin()

    fun saveThemePreference(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }

    fun isDarkThemeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("dark_theme", false)
    }

    // --- Напоминания ---

    fun isReminderEnabled(): Boolean = sessionManager.isReminderEnabled()

    fun getReminderHour(): Int = sessionManager.getReminderHour()
    fun getReminderMinute(): Int = sessionManager.getReminderMinute()

    fun setReminderEnabled(enabled: Boolean) {
        sessionManager.setReminderEnabled(enabled)
        val context = getApplication<Application>()
        if (enabled) {
            NotificationScheduler.scheduleDailyReminder(
                context,
                sessionManager.getReminderHour(),
                sessionManager.getReminderMinute()
            )
        } else {
            NotificationScheduler.cancelReminder(context)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        sessionManager.setReminderTime(hour, minute)
        if (sessionManager.isReminderEnabled()) {
            NotificationScheduler.scheduleDailyReminder(getApplication(), hour, minute)
        }
    }

    // --- Отчёты ---

    fun exportCsv() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            _exportError.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            try {
                val sugarNotes = sugarRepository.getAllSugarNotesByUserId(userId)
                val insulinNotes = insulinRepository.getAllInsulinNotesByUserId(userId)
                val file = reportManager.exportCsv(sugarNotes, insulinNotes)
                _exportedFile.postValue(file)
            } catch (e: Exception) {
                _exportError.postValue("Ошибка экспорта CSV: ${e.message}")
            }
        }
    }

    fun exportPdf() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            _exportError.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            try {
                val sugarNotes = sugarRepository.getAllSugarNotesByUserId(userId)
                val insulinNotes = insulinRepository.getAllInsulinNotesByUserId(userId)
                val file = reportManager.exportPdf(sugarNotes, insulinNotes, getUserName())
                _exportedFile.postValue(file)
            } catch (e: Exception) {
                _exportError.postValue("Ошибка экспорта PDF: ${e.message}")
            }
        }
    }

    fun clearExportedFile() {
        _exportedFile.value = null
    }

    fun clearExportError() {
        _exportError.value = null
    }
}
