// dev.moorhen.diahelp.data.repository.InsulinRepository
package dev.moorhen.diahelp.data.repository

import android.content.Context
import dev.moorhen.diahelp.data.db.AppDatabase
import dev.moorhen.diahelp.data.model.InsulinModel
import java.util.*

class InsulinRepository(context: Context) {

    private val dao = AppDatabase.getDatabase(context).insulinDao()

    suspend fun insert(note: InsulinModel) = dao.insert(note)

    suspend fun getAllInsulinNotesByUserId(userId: Int) = dao.getAllByUserId(userId)

    suspend fun clearAllForUser(userId: Int) = dao.clearByUserId(userId)

    suspend fun getNotesByPeriod(userId: Int, startDate: Date, endDate: Date) = dao.getByPeriod(userId, startDate, endDate)

    suspend fun getAverageInsulinPerDayForUser(userId: Int): Double? {
        return dao.getAverageInsulinPerDayForUser(userId)
    }

    suspend fun getInsulinDataByPeriod(userId: Int, startDate: Date, endDate: Date): List<Pair<Long, Double>> {
        return getNotesByPeriod(userId, startDate, endDate)
            .map { Pair(it.Date.time, it.InsulinDose) }
            .sortedBy { it.first }
    }
}