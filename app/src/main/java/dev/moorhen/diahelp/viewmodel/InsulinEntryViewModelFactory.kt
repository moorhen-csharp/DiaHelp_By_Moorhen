// dev.moorhen.diahelp.viewmodel.InsulinEntryViewModelFactory
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.utils.SessionManager

class InsulinEntryViewModelFactory(
    private val repository: InsulinRepository,
    private val app: Application,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsulinEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsulinEntryViewModel(repository, app, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}