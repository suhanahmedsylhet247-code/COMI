package com.comi.reader.data.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val updateChecker: UpdateChecker,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            updateChecker.checkForUpdates()
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("UpdateCheckWorker", "Update check failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "chapter_update_check"

        fun schedule(context: Context, intervalHours: Long = 6) {
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                intervalHours, TimeUnit.HOURS,
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
