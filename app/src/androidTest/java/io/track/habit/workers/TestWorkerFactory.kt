package io.track.habit.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import io.track.habit.data.repository.StreakRepository
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.repository.HabitRepository

class TestWorkerFactory(
    private val streakRepository: StreakRepository,
    private val habitRepository: HabitRepository,
    private val settingsDataStore: SettingsDataStore,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            StreakNotifierWorker::class.java.name -> StreakNotifierWorker(
                context = appContext,
                workerParameters = workerParameters,
                habitRepository = habitRepository,
                settingsDataStore = settingsDataStore,
                streakRepository = streakRepository,
            )
            else -> null
        }
    }
}
