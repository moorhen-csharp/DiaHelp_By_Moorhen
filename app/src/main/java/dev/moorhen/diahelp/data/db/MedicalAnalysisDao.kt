package dev.moorhen.diahelp.data.db

import androidx.room.*
import dev.moorhen.diahelp.data.model.MedicalAnalysisModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalAnalysisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicalAnalysisModel): Long

    @Query("SELECT * FROM medical_analysis_records WHERE userId = :userId ORDER BY date DESC")
    fun getAllByUserId(userId: Int): Flow<List<MedicalAnalysisModel>>

    @Query("SELECT * FROM medical_analysis_records WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestByUserId(userId: Int): MedicalAnalysisModel?

    @Query("SELECT * FROM medical_analysis_records WHERE userId = :userId AND analysisType = :type ORDER BY date DESC LIMIT 1")
    suspend fun getLatestByType(userId: Int, type: String): MedicalAnalysisModel?

    @Query("SELECT * FROM medical_analysis_records WHERE syncedToHc = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: Int): List<MedicalAnalysisModel>

    @Query("UPDATE medical_analysis_records SET syncedToHc = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)

    @Delete
    suspend fun delete(record: MedicalAnalysisModel)
}
