// dev.moorhen.diahelp.viewmodel.SugarNoteViewModelFactory
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.utils.SessionManager

class SugarNoteViewModelFactory(
    private val sugarRepository: SugarRepository,
    private val insulinRepository: InsulinRepository, // ✅ Новый репозиторий
    private val app: Application,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SugarNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SugarNoteViewModel(sugarRepository, insulinRepository, app, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}