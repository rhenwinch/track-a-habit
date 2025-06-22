package io.track.habit.workers

import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.datastore.entities.UserAppStateRegistry
import io.track.habit.data.repository.StreakRepository
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.workers.utils.StreakNotificationUtils
import io.track.habit.workers.utils.StreakNotificationUtils.createNotification
import io.track.habit.workers.utils.StreakNotificationUtils.createNotificationChannel
import io.track.habit.workers.utils.StreakNotificationUtils.getNotificationManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class StreakNotifierWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted workerParameters: WorkerParameters,
        private val habitRepository: HabitRepository,
        private val settingsDataStore: SettingsDataStore,
        private val streakRepository: StreakRepository,
    ) : CoroutineWorker(context, workerParameters) {
        private val gson by lazy { Gson() }

        override suspend fun doWork(): Result {
            return checkStreaksAndNotify()
        }

        private suspend fun checkStreaksAndNotify(): Result {
            val streaks = streakRepository.getAllStreaks()
            val habits = habitRepository.getAllHabits().first()

            val habitsToReachNewMilestones = streaks.flatMap { streak ->
                habits.filter { habit ->
                    habit.streakInDays >= (streak.minDaysRequired * STREAK_REVEAL_PERCENTAGE_THRESHOLD).toInt() &&
                        habit.streakInDays < streak.maxDaysRequired
                }
            }

            if (habitsToReachNewMilestones.isEmpty()) {
                return Result.success()
            }

            val userAppState = settingsDataStore.appStateFlow.first()
            val previousStreaksNotified = userAppState
                .lastNotifiedStreaks
                .fromJson()

            if (habitsToReachNewMilestones == previousStreaksNotified) {
                return Result.success()
            }

            settingsDataStore.updateSetting(
                definition = UserAppStateRegistry.LAST_NOTIFIED_STREAKS,
                value = habitsToReachNewMilestones.toJson(),
            )

            val notificationManager = applicationContext.getNotificationManager()
            if (!notificationManager.areNotificationsEnabled()) return Result.success()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.createNotificationChannel()
            }

            val count = habitsToReachNewMilestones.size
            val notification = if (count == 1) {
                val generalSettings = settingsDataStore.generalSettingsFlow.first()

                applicationContext.createNotification(
                    habit = habitsToReachNewMilestones.first(),
                    isCensored = generalSettings.censorHabitNames,
                )
            } else {
                applicationContext.createNotification(count)
            }

            notificationManager.notify(
                // id =
                StreakNotificationUtils.NOTIFICATION_ID,
                // notification =
                notification,
            )

            return Result.success()
        }

        private fun String.fromJson(): List<Habit> {
            return gson.fromJson(this, Array<Habit>::class.java).toList()
        }

        private fun List<Habit>.toJson(): String {
            return gson.toJson(this)
        }

        companion object {
            private const val STREAK_REVEAL_PERCENTAGE_THRESHOLD = 0.98f
            private const val NAME = "StreakNotifierWorker"

            fun schedule(context: Context): Operation {
                val constraints = Constraints
                    .Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

                val periodicRequest = PeriodicWorkRequestBuilder<StreakNotifierWorker>(
                    repeatInterval = 6,
                    repeatIntervalTimeUnit = TimeUnit.DAYS,
                ).setConstraints(constraints)
                    .addTag(NAME)
                    .setBackoffCriteria(
                        backoffPolicy = BackoffPolicy.LINEAR,
                        backoffDelay = PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
                        timeUnit = TimeUnit.MILLISECONDS,
                    ).build()

                return WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    uniqueWorkName = NAME,
                    existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                    request = periodicRequest,
                )
            }

            fun cancel(context: Context): Operation {
                return WorkManager.getInstance(context).cancelUniqueWork(NAME)
            }
        }
    }
