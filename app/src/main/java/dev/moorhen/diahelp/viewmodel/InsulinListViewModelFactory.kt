// dev.moorhen.diahelp.viewmodel.InsulinListViewModelFactory
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.utils.SessionManager

class InsulinListViewModelFactory(
    private val repository: InsulinRepository,
    private val app: Application,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsulinListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsulinListViewModel(repository, app, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}