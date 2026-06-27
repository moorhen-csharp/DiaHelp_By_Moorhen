// dev.moorhen.diahelp.viewmodel.CorrectionViewModel
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.moorhen.diahelp.data.db.AppDatabase
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.model.SugarModel
import dev.moorhen.diahelp.data.repository.InsulinRepository // ✅ Добавьте импорт
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.repository.UserRepository
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class CorrectionViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(AppDatabase.getDatabase(application).userDao())
    private val sessionManager = SessionManager(application)

    // ✅ Создаем репозитории для сахара и инсулина
    private val sugarRepository = SugarRepository(application)
    private val insulinRepository = InsulinRepository(application) // ✅

    private val _correctionResult = MutableLiveData<String>()
    val correctionResult: LiveData<String> = _correctionResult

    private val _showDialog = MutableLiveData<Pair<Double, Double>>()
    val showDialog: LiveData<Pair<Double, Double>> = _showDialog

    fun calculateInsulin(currentGlucose: Double, targetGlucose: Double) {
        viewModelScope.launch {
            val username = sessionManager.getUsername()
            val user = withContext(Dispatchers.IO) {
                if (username != null)
                    userRepository.getUserByUsernameOrEmail(username, username)
                else null
            }

            val coeffInsulin = user?.coeffInsulin?.takeIf { it > 0 } ?: 2.0
            val rawCorrection = (currentGlucose - targetGlucose) / coeffInsulin
            // Округляем до 1 знака, чтобы избежать ошибок Double (например, 6.300000000000001)
            val finalValue = if (rawCorrection < 0) 0.0
            else Math.round(rawCorrection * 10.0) / 10.0

            _correctionResult.postValue("%.1f".format(finalValue))
            _showDialog.postValue(Pair(currentGlucose, finalValue))

        }
    }

    // ✅ Исправленная функция сохранения
    fun saveSugarNote(sugarLevel: Double, insulinDose: Double) {
        val context = getApplication<Application>().applicationContext
        val userId = sessionManager.getUserId()

        if (userId == -1) {
            Toast.makeText(context, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Сохраняем запись о сахаре
        val sugarNote = SugarModel(
            userId = userId,
            SugarLevel = sugarLevel,
            MeasurementTime = "Коррекция",
            HealthType = "Не указано",
            InsulinDose = insulinDose,
            Date = Date()
        )

        // 2. Сохраняем запись об инсулине (если доза больше 0)
        if (insulinDose > 0) {
            val insulinNote = InsulinModel(
                userId = userId,
                InsulinDose = insulinDose,
                Date = Date()
            )

            // Запускаем вставку обоих записей в базу данных
            viewModelScope.launch {
                try {
                    // Сначала сохраняем сахар
                    sugarRepository.insert(sugarNote)
                    // Затем сохраняем инсулин
                    insulinRepository.insert(insulinNote)
                    // Можно показать Toast об успехе здесь, если нужно
                } catch (e: Exception) {
                    // Обработка ошибок (опционально, но рекомендуется)
                    Toast.makeText(context, "Ошибка при сохранении: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Если инсулин не вводился, сохраняем только сахар
            viewModelScope.launch {
                try {
                    sugarRepository.insert(sugarNote)
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка при сохранении сахара: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}