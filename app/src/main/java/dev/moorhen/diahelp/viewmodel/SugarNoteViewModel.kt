// dev.moorhen.diahelp.viewmodel.SugarNoteViewModel
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.model.SugarModel
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.*

class SugarNoteViewModel(
    private val sugarRepository: SugarRepository,
    private val insulinRepository: InsulinRepository, // ✅ Новый репозиторий
    app: Application,
    private val sessionManager: SessionManager
) : AndroidViewModel(app) {

    // LiveData для сахара
    val sugarNotes = MutableLiveData<List<SugarModel>>()

    // LiveData для инсулина
    val insulinNotes = MutableLiveData<List<InsulinModel>>() // ✅ Новый список

    // Среднее значение (общее, но будет обновляться в зависимости от выбранного режима)
    val average = MutableLiveData<Double>()

    // Выбранный период
    val selectedPeriod = MutableLiveData("1 День")

    // Флаг, какой список показывать
    val isSugarListVisible = MutableLiveData(true)

    // Пуст ли текущий список
    val isEmpty = MutableLiveData(true)

    init {
        loadSugarNotes()
    }

    // Загрузка записей сахара
    fun loadSugarNotes() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            sugarNotes.postValue(emptyList())
            isEmpty.postValue(true)
            return
        }

        viewModelScope.launch {
            val list = sugarRepository.getAllSugarNotesByUserId(userId)
            sugarNotes.postValue(list)
            isEmpty.postValue(list.isEmpty())
            calculateAverageForSugar() // Рассчитываем среднее для сахара
        }
    }

    // Загрузка записей инсулина
    fun loadInsulinNotes() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            insulinNotes.postValue(emptyList())
            isEmpty.postValue(true)
            return
        }

        viewModelScope.launch {
            val list = insulinRepository.getAllInsulinNotesByUserId(userId)
            insulinNotes.postValue(list)
            isEmpty.postValue(list.isEmpty())
            calculateAverageForInsulin() // Рассчитываем среднее для инсулина
        }
    }

    // Очистка записей (в зависимости от выбранного режима)
    fun clearNotes() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            if (isSugarListVisible.value == true) {
                sugarRepository.clearAllForUser(userId)
                loadSugarNotes()
            } else {
                insulinRepository.clearAllForUser(userId)
                loadInsulinNotes()
            }
        }
    }

    // Расчет среднего для сахара по периоду
    fun calculateAverageForSugar() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            average.postValue(0.0)
            return
        }

        viewModelScope.launch {
            val endDate = Date()
            val startDate = getStartDateForPeriod(selectedPeriod.value ?: "1 День")
            val notes = sugarRepository.getNotesByPeriod(userId, startDate, endDate)
            val valid = notes.filter { it.SugarLevel != -1.0 }
            val avg = if (valid.isNotEmpty()) valid.map { it.SugarLevel }.average() else 0.0
            average.postValue(avg)
        }
    }

    // Расчет среднего для инсулина по периоду
    fun calculateAverageForInsulin() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            average.postValue(0.0)
            return
        }

        viewModelScope.launch {
            val endDate = Date()
            val startDate = getStartDateForPeriod(selectedPeriod.value ?: "1 День")
            val notes = insulinRepository.getNotesByPeriod(userId, startDate, endDate)
            val avg = if (notes.isNotEmpty()) notes.map { it.InsulinDose }.average() else 0.0
            average.postValue(avg)
        }
    }

    // Вспомогательная функция для расчета даты начала периода
    private fun getStartDateForPeriod(period: String): Date {
        val endDate = Date()
        return when (period) {
            "1 День" -> Date(endDate.time - 86400000L)
            "1 Неделя" -> Date(endDate.time - 7L * 86400000L)
            "1 Месяц" -> Date(endDate.time - 30L * 86400000L)
            "3 Месяца" -> Date(endDate.time - 90L * 86400000L)
            "6 Месяцев" -> Date(endDate.time - 180L * 86400000L)
            "1 Год" -> Date(endDate.time - 365L * 86400000L)
            else -> Date()
        }
    }

    // Переключение на Сахар
    fun toggleToSugar() {
        isSugarListVisible.value = true
        calculateAverageForSugar()
    }

    // Переключение на Инсулин
    fun toggleToInsulin() {
        isSugarListVisible.value = false
        calculateAverageForInsulin()
    }

    // Обновление среднего значения при смене периода
    fun onPeriodChanged() {
        if (isSugarListVisible.value == true) {
            calculateAverageForSugar()
        } else {
            calculateAverageForInsulin()
        }
    }
}