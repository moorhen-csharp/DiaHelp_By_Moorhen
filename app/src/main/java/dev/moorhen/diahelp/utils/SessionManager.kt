package dev.moorhen.diahelp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id" // <-- НОВАЯ КОНСТАНТА
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_COEFF_INSULIN = "coeffInsulin"
        private const val KEY_STREAK = "no_sugar_streak"
        private const val KEY_LAST_ASK_DATE = "last_ask_date"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
    }

    // Обновленная сигнатура: принимает userId
    fun saveUser(userId: Int, username: String, email: String, coeffInsulin: Double) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId) // <-- СОХРАНЯЕМ ID
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putFloat(KEY_COEFF_INSULIN, coeffInsulin.toFloat())
            apply()
        }
    }

    // Новый метод: получить ID текущего пользователя
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1) // Возвращает -1, если не залогинен

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getCoeffInsulin(): Double {
        return prefs.getFloat(KEY_COEFF_INSULIN, 0f).toDouble()
    }

    fun getStreak(): Int = prefs.getInt(KEY_STREAK, 0)

    fun saveStreak(value: Int) {
        prefs.edit().putInt(KEY_STREAK, value).apply()
    }

    fun getLastAskDate(): String? = prefs.getString(KEY_LAST_ASK_DATE, null)

    fun saveLastAskDate(date: String) {
        prefs.edit().putString(KEY_LAST_ASK_DATE, date).apply()
    }

    // Обновленная проверка: теперь проверяет наличие userId
    fun isLoggedIn(): Boolean = getUserId() != -1 // Вместо getUsername() != null

    // --- Настройки напоминаний ---
    fun isReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_ENABLED, false)

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    fun getReminderHour(): Int = prefs.getInt(KEY_REMINDER_HOUR, 9)
    fun getReminderMinute(): Int = prefs.getInt(KEY_REMINDER_MINUTE, 0)

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}