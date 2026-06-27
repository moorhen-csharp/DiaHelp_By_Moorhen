package dev.moorhen.diahelp.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Управляет планированием фоновой синхронизации с Health Connect.
 * WorkManager не позволяет периодическим задачам выполняться чаще,
 * чем раз в 15 минут — используем 4 часа как разумный компромисс
 * между свежестью данных и расходом батареи.
 *
 * ВАЖНО: используем ExistingPeriodicWorkPolicy.UPDATE вместо KEEP,
 * чтобы worker не «зависал» после нескольких дней работы.
 * UPDATE обновляет расписание при каждом запуске приложения,
 * сохраняя при этом счётчик повторов и статус.
 */
object HcSyncScheduler {

    private const val SYNC_INTERVAL_HOURS = 4L

    /** Запускает или обновляет периодическую синхронизацию с Health Connect. */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<HcSyncWorker>(SYNC_INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        // UPDATE вместо KEEP: обновляет worker при каждом старте приложения,
        // исправляет зависание после 2–3 дней без перезапуска
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HcSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** Останавливает фоновую синхронизацию. */
    fun cancelPeriodicSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(HcSyncWorker.WORK_NAME)
    }

    /** Запускает разовую синхронизацию немедленно (например, сразу после выдачи разрешений). */
    fun triggerImmediateSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<HcSyncWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
