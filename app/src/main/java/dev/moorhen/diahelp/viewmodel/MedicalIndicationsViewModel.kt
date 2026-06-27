package dev.moorhen.diahelp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.moorhen.diahelp.data.model.MedicalAnalysisModel
import dev.moorhen.diahelp.data.repository.MedicalAnalysisRepository
import dev.moorhen.diahelp.utils.HealthConnectManager
import dev.moorhen.diahelp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicalIndicationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MedicalAnalysisRepository(application)
    private val sessionManager = SessionManager(application)

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _latestRecord = MutableLiveData<MedicalAnalysisModel?>()
    val latestRecord: LiveData<MedicalAnalysisModel?> = _latestRecord

    fun loadLatest(type: String) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId == -1) return@launch
            val record = withContext(Dispatchers.IO) {
                repository.getLatestByType(userId, type)
            }
            _latestRecord.postValue(record)
        }
    }

    fun saveAnalysis(
        type: String,
        hba1c: Double? = null,
        cpeptide: Double? = null,
        hemoglobin: Double? = null,
        leukocytes: Double? = null,
        platelets: Double? = null,
        cholesterol: Double? = null,
        hdl: Double? = null,
        ldl: Double? = null,
        triglycerides: Double? = null,
        creatinine: Double? = null,
        urea: Double? = null,
        alt: Double? = null,
        ast: Double? = null
    ) {
        val context = getApplication<Application>().applicationContext
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(context, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            _saveSuccess.postValue(false)
            return
        }

        val record = MedicalAnalysisModel(
            userId = userId,
            analysisType = type,
            hba1c = hba1c,
            cpeptide = cpeptide,
            hemoglobin = hemoglobin,
            leukocytes = leukocytes,
            platelets = platelets,
            cholesterol = cholesterol,
            hdl = hdl,
            ldl = ldl,
            triglycerides = triglycerides,
            creatinine = creatinine,
            urea = urea,
            alt = alt,
            ast = ast
        )

        viewModelScope.launch {
            try {
                val id = withContext(Dispatchers.IO) { repository.save(record) }

                // Пытаемся синхронизировать HbA1c в Health Connect
//                if (hba1c != null) {
//                    syncHba1cToHc(id.toInt(), hba1c)
//                }

                _saveSuccess.postValue(true)
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                _saveSuccess.postValue(false)
            }
        }
    }

//    private suspend fun syncHba1cToHc(recordId: Int, hba1c: Double) {
//        val context = getApplication<Application>().applicationContext
//        try {
//            val client = HealthConnectManager.getClientOrNull(context) ?: return
//            if (!HealthConnectManager.hasAllPermissions(client)) return
////            HealthConnectManager.writeHba1c(client, hba1c)
//            repository.markSynced(recordId)
//        } catch (_: Exception) {
//            // Запись останется с syncedToHc=false — синхронизируется при следующем фоновом worker'е
//        }
//    }
}
