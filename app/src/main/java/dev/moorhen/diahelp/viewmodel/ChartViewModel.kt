package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Date

class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val sugarRepository = SugarRepository(application)
    private val insulinRepository = InsulinRepository(application)
    private val sessionManager = SessionManager(application)


    val average = MutableLiveData<Double>()

    // (timestamp в мс, значение)
    val sugarData = MutableLiveData<List<Pair<Long, Double>>>()
    val insulinData = MutableLiveData<List<Pair<Long, Double>>>()

    val isEmpty = MutableLiveData(true)

    // Выбранный период (как в SugarNoteViewModel)
    val selectedPeriod = MutableLiveData("1 Неделя")

    // true = показываем сахар, false = инсулин
    val isSugarMode = MutableLiveData(true)

    init {
        loadData()
        calculateAverageForSugar()
    }

    fun setPeriod(period: String) {
        selectedPeriod.value = period
        loadData()
        calculateAverageForSugar()
    }

    fun toggleMode(showSugar: Boolean) {
        isSugarMode.value = showSugar
    }

    fun loadData() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            sugarData.postValue(emptyList())
            insulinData.postValue(emptyList())
            isEmpty.postValue(true)
            return
        }

        viewModelScope.launch {
            val endDate = Date()
            val startDate = getStartDateForPeriod(selectedPeriod.value ?: "1 Неделя")

            val sugar = sugarRepository.getSugarDataByPeriod(userId, startDate, endDate)
            val insulin = insulinRepository.getInsulinDataByPeriod(userId, startDate, endDate)

            sugarData.postValue(sugar)
            insulinData.postValue(insulin)
            isEmpty.postValue(sugar.isEmpty() && insulin.isEmpty())
        }
    }

    fun calculateAverageForSugar() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            average.postValue(0.0)
            return
        }

        viewModelScope.launch {
            val endDate = Date()
            val startDate = getStartDateForPeriod(selectedPeriod.value ?: "1 Неделя")
            val notes = sugarRepository.getNotesByPeriod(userId, startDate, endDate)
            val valid = notes.filter { it.SugarLevel != -1.0 }
            val avg = if (valid.isNotEmpty()) valid.map { it.SugarLevel }.average() else 0.0
            average.postValue(avg)
        }
    }

    private fun getStartDateForPeriod(period: String): Date {
        val endDate = Date()
        return when (period) {
            "1 День" -> Date(endDate.time - 86400000L)
            "1 Неделя" -> Date(endDate.time - 7L * 86400000L)
            "1 Месяц" -> Date(endDate.time - 30L * 86400000L)
            "3 Месяца" -> Date(endDate.time - 90L * 86400000L)
            "6 Месяцев" -> Date(endDate.time - 180L * 86400000L)
            "1 Год" -> Date(endDate.time - 365L * 86400000L)
            else -> Date(endDate.time - 7L * 86400000L)
        }
    }
}
