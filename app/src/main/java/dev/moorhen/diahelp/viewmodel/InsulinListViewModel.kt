// dev.moorhen.diahelp.viewmodel.InsulinListViewModel
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.launch

class InsulinListViewModel(
    private val repository: InsulinRepository,
    app: Application,
    private val sessionManager: SessionManager
) : AndroidViewModel(app) {

    val insulinNotes = MutableLiveData<List<InsulinModel>>()
    val average = MutableLiveData<Double>()
    val isEmpty = MutableLiveData(true)

    init {
        loadAllData()
    }

    fun loadAllData() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            insulinNotes.postValue(emptyList())
            average.postValue(0.0)
            isEmpty.postValue(true)
            return
        }

        viewModelScope.launch {
            // Загружаем список записей
            val list = repository.getAllInsulinNotesByUserId(userId)
            insulinNotes.postValue(list)
            isEmpty.postValue(list.isEmpty())

            // Сразу же рассчитываем и показываем среднее значение
            calculateAverageInsulinPerDay(userId)
        }
    }

    // Приватный метод для расчета среднего
    private fun calculateAverageInsulinPerDay(userId: Int) {
        viewModelScope.launch {
            val avg = repository.getAverageInsulinPerDayForUser(userId)
            average.postValue(avg ?: 0.0)
        }
    }

    fun clearNotes() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            repository.clearAllForUser(userId)
            loadAllData() // После очистки перезагружаем все данные
        }
    }
}