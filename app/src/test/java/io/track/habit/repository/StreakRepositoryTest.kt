package io.track.habit.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.mockk.coEvery
import io.mockk.mockk
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.StreakRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@Suppress("ktlint:standard:function-naming")
class StreakRepositoryTest {
    private val gson = Gson()
    private lateinit var streaks: List<Streak>

    private val repository = mockk<StreakRepository>()

    @Before
    fun setUp() {
        val jsonContent = """
            [
                {
                    "title": "Getting Started",
                    "maxDaysRequired": 6,
                    "minDaysRequired": 0,
                    "badgeIcon": "ic_bronze_badge",
                    "message": "Great start!"
                },
                {
                    "title": "One Week Streak",
                    "maxDaysRequired": 13,
                    "minDaysRequired": 7,
                    "badgeIcon": "ic_week_streak_badge",
                    "message": "You're on fire!"
                },
                {
                    "title": "Two Week Streak",
                    "maxDaysRequired": 20,
                    "minDaysRequired": 14,
                    "badgeIcon": "ic_two_week_streak_badge",
                    "message": "Keeping up the momentum!"
                },
                {
                    "title": "One Month Streak",
                    "maxDaysRequired": 34,
                    "minDaysRequired": 21,
                    "badgeIcon": "ic_month_streak_badge",
                    "message": "Impressive dedication!"
                },
                {
                    "title": "Champion",
                    "maxDaysRequired": 59,
                    "minDaysRequired": 35,
                    "badgeIcon": "ic_champion_badge",
                    "message": "You're a true champion!"
                },
                {
                    "title": "Legend",
                    "maxDaysRequired": 10000,
                    "minDaysRequired": 60,
                    "badgeIcon": "ic_legend_badge",
                    "message": "You're a legend!"
                }
            ]
        """

        streaks = gson.fromJson(jsonContent)

        coEvery { repository.getAllStreaks() } returns streaks
        coEvery { repository.getStreakByDaysRequired(any()) } answers {
            val days = arg<Int>(0)
            streaks.find { days >= it.minDaysRequired && days <= it.maxDaysRequired }
                ?: throw IllegalArgumentException("No streak found for $days days")
        }
    }

    private fun Gson.fromJson(json: String): List<Streak> {
        val type = object : TypeToken<List<Streak>>() {}.type
        return fromJson(json, type)
    }

    @Test
    fun `getStreakByDaysRequired returns correct streak`() =
        runTest {
            val days = 10
            val streak = repository.getStreakByDaysRequired(days)
            assert(days >= streak.minDaysRequired && days <= streak.maxDaysRequired)
        }
}
