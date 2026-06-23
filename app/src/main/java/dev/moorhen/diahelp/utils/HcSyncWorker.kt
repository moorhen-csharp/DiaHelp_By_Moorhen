package dev.moorhen.diahelp.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.moorhen.diahelp.data.repository.SugarRepository

/**
 * Фоновая задача периодической синхронизации с Health Connect.
 *
 * Делает две вещи:
 *  1. Импортирует новые записи BloodGlucoseRecord из Health Connect в локальную БД
 *     (с защитой от дублей по userId+Date).
 *  2. Дополнительно пытается экспортировать локальные записи DiaHelp в Health Connect —
 *     на случай, если "живой" экспорт при сохранении записи (см. SugarEntryViewModel)
 *     не удался из-за временного отсутствия разрешений.
 */
class HcSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "diahelp_hc_sync"
    }

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val client = HealthConnectManager.getClientOrNull(context) ?: return Result.success()

            if (!HealthConnectManager.hasAllPermissions(client)) {
                // Нет разрешений — не считаем это ошибкой, просто пропускаем синхронизацию
                return Result.success()
            }

            val sessionManager = SessionManager(context)
            val userId = sessionManager.getUserId()
            if (userId == -1) return Result.success()

            val sugarRepository = SugarRepository(context)

            // ─── Импорт: HC → DiaHelp ───────────────────────────────
            val imported = HealthConnectManager.readBloodGlucose(client, userId)
            imported.forEach { note ->
                sugarRepository.insertIfNotExists(note)
            }

            // ─── Экспорт: DiaHelp → HC (страхуем несинхронизированные записи) ───
            val localNotes = sugarRepository.getAllSugarNotesByUserId(userId)
            HealthConnectManager.writeSugarNotes(client, localNotes)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
