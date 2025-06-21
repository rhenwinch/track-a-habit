package io.track.habit

import android.app.Application
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.track.habit.di.DefaultDispatcher
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.workers.StreakNotifierWorker
import io.track.habit.workers.utils.StreakNotificationUtils
import io.track.habit.workers.utils.StreakNotificationUtils.deleteNotificationChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TaHApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @DefaultDispatcher
    @Inject
    lateinit var defaultDispatcher: CoroutineDispatcher

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(defaultDispatcher + SupervisorJob()).launch {
            settingsDataStore.generalSettingsFlow.collectLatest {
                if (it.notificationsEnabled) {
                    StreakNotifierWorker.schedule(this@TaHApplication)
                } else {
                    StreakNotifierWorker.cancel(this@TaHApplication)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        this@TaHApplication.deleteNotificationChannel(StreakNotificationUtils.CHANNEL_ID)
                    }
                }
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
