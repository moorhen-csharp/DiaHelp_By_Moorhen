// dev.moorhen.diahelp.viewmodel.InsulinEntryViewModel
package dev.moorhen.diahelp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.*

class InsulinEntryViewModel(
    private val repository: InsulinRepository,
    app: Application,
    private val sessionManager: SessionManager
) : AndroidViewModel(app) {

    var insulinDose: Double? = null

    fun saveNote(): Boolean {
        val ctx = getApplication<Application>()
        val userId = sessionManager.getUserId()

        if (userId == -1) {
            Toast.makeText(ctx, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return false
        }

        if (insulinDose == null || insulinDose!! <= 0) {
            Toast.makeText(ctx, "Введите корректную дозу инсулина", Toast.LENGTH_SHORT).show()
            return false
        }

        val note = InsulinModel(
            userId = userId,
            InsulinDose = insulinDose!!,
            Date = Date()
        )

        viewModelScope.launch {
            repository.insert(note)
        }

        return true
    }
}