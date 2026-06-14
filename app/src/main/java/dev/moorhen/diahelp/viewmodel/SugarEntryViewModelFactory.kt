// dev.moorhen.diahelp.viewmodel.SugarEntryViewModelFactory
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.moorhen.diahelp.data.repository.InsulinRepository // ✅ Добавьте импорт
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.utils.SessionManager

class SugarEntryViewModelFactory(
    private val repository: SugarRepository,
    private val insulinRepository: InsulinRepository, // ✅ Добавьте параметр
    private val app: Application,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SugarEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // ✅ Передаем ВСЕ 4 параметра
            return SugarEntryViewModel(repository, insulinRepository, app, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}