package dev.moorhen.diahelp.data.db

import androidx.room.*
import dev.moorhen.diahelp.data.model.InsulinModel
import java.util.*

@Dao
interface InsulinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: InsulinModel)

    @Query("SELECT * FROM insulin_notes WHERE userId = :userId ORDER BY Date DESC")
    suspend fun getAllByUserId(userId: Int): List<InsulinModel>

    @Query("DELETE FROM insulin_notes")
    suspend fun clear()

    @Query("SELECT * FROM insulin_notes WHERE userId = :userId AND Date BETWEEN :startDate AND :endDate ORDER BY Date DESC")
    suspend fun getByPeriod(userId: Int, startDate: Date, endDate: Date): List<InsulinModel>

    @Delete
    suspend fun delete(note: InsulinModel)

    @Query("DELETE FROM insulin_notes WHERE userId = :userId")
    suspend fun clearByUserId(userId: Int)

    // Новый запрос для расчета среднего инсулина в день
    @Query("""
        SELECT AVG(daily_total) 
        FROM (
            SELECT DATE(Date / 1000, 'unixepoch', 'localtime') as day, SUM(InsulinDose) as daily_total
            FROM insulin_notes
            WHERE userId = :userId
            GROUP BY day
        )
    """)
    suspend fun getAverageInsulinPerDayForUser(userId: Int): Double?
}