package dev.moorhen.diahelp.data.repository

import android.content.Context
import dev.moorhen.diahelp.data.db.AppDatabase
import dev.moorhen.diahelp.data.model.MedicalAnalysisModel
import kotlinx.coroutines.flow.Flow

class MedicalAnalysisRepository(context: Context) {

    private val dao = AppDatabase.getDatabase(context).medicalAnalysisDao()

    suspend fun save(record: MedicalAnalysisModel): Long = dao.insert(record)

    fun getAllByUserId(userId: Int): Flow<List<MedicalAnalysisModel>> =
        dao.getAllByUserId(userId)

    suspend fun getLatestByType(userId: Int, type: String): MedicalAnalysisModel? =
        dao.getLatestByType(userId, type)

    suspend fun getUnsynced(userId: Int): List<MedicalAnalysisModel> =
        dao.getUnsynced(userId)

    suspend fun markSynced(id: Int) = dao.markSynced(id)
}
