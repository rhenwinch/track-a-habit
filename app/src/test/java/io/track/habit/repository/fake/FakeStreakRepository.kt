package io.track.habit.repository.fake

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.StreakRepository

class FakeStreakRepository : StreakRepository {
    private val gson = Gson()
    private val streaks: List<Streak>

    init {
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
    }

    override fun getAllStreaks(): List<Streak> {
        return streaks
    }

    private fun Gson.fromJson(json: String): List<Streak> {
        val type = object : TypeToken<List<Streak>>() {}.type
        return fromJson(json, type)
    }
}
