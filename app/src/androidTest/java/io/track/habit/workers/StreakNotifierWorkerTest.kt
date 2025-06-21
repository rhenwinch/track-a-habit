package io.track.habit.workers

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.repository.StreakRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.util.Date
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class StreakNotifierWorkerTest {
    private lateinit var context: Context

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var streaksRepository: StreakRepository

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    @Throws(Exception::class)
    fun testStreakNotifierWorker() =
        runTest {
            val streaks = streaksRepository.getAllStreaks()
            val demoStreak = streaks.first()

            val habitStartedAt =
                Date(System.currentTimeMillis() - ((demoStreak.maxDaysRequired - 2) * 24 * 60 * 60 * 1000L))
            val habitId = habitRepository.insertHabit(
                Habit(
                    name = "Test Habit",
                    createdAt = habitStartedAt,
                ),
            )

            val habit = habitRepository.getHabitById(habitId)
            expectThat(habit).isNotNull()

            val worker = TestListenableWorkerBuilder<StreakNotifierWorker>(context)
                .setWorkerFactory(
                    TestWorkerFactory(
                        habitRepository = habitRepository,
                        settingsDataStore = settingsDataStore,
                        streakRepository = streaksRepository,
                    ),
                ).build()

            val result = worker.doWork()
            expectThat(result).isEqualTo(ListenableWorker.Result.success())

            habitRepository.deleteHabit(habit!!)

            val deletedHabit = habitRepository.getHabitById(habitId)
            expectThat(deletedHabit).isNull()
        }
}
