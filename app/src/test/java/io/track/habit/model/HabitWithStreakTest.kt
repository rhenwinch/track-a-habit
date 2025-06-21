package io.track.habit.model

import io.mockk.mockk
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.Habit.Companion.getName
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Streak
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class HabitWithStreakTest {
    @Test
    fun `formattedActiveSinceDate returns correct format on SDK 26 and above (inclusive)`() {
        val testDate = GregorianCalendar(2024, Calendar.MARCH, 5, 9, 30).time
        val habit = Habit(name = "Read", lastResetAt = testDate)
        val streak = mockk<Streak>()
        val habitWithStreak = HabitWithStreak(habit, streak)

        val formatted = habitWithStreak.formattedActiveSinceDate
        println("Formatted date: $formatted")
        assertTrue("Expected format to include AM/PM", formatted.endsWith("AM") || formatted.endsWith("PM"))
        assertTrue("Expected format to contain slash", formatted.contains("/"))
        assertTrue("Expected format to contain colon", formatted.contains(":"))
    }

    @Test
    fun `getCensoredHabit censors correctly short names`() {
        var name = "Hi"
        while (name.isNotEmpty()) {
            val habit = Habit(name = name)
            val streak = mockk<Streak>()
            val original = HabitWithStreak(habit, streak)

            val censored = original.habit.getName(true)

            println("Actual habit name: $name")
            println("Censored habit name: $censored")

            name = name.dropLast(1)
        }
    }

    @Test
    fun `getCensoredHabit censors correctly long names`() {
        val habit = Habit(name = "ExerciseDaily")
        val streak = mockk<Streak>()
        val original = HabitWithStreak(habit, streak)

        val censored = original.habit.getName(true)

        // Take 2 characters and pad to 8
        println("Censored habit name: $censored")
        assertEquals("Ex******", censored)
    }

    @Test
    fun `getCensoredHabit does not censor when censor is false`() {
        val habit = Habit(name = "ExerciseDaily")
        val streak = mockk<Streak>()
        val original = HabitWithStreak(habit, streak)

        val censored = original.habit.getName(false)

        println("Censored habit name: $censored")
        assertEquals("ExerciseDaily", censored)
    }

    private fun createHabitWithLastResetAt(timeAgo: Long): HabitWithStreak {
        val currentTime = System.currentTimeMillis()
        val lastResetAt = Date(currentTime - timeAgo)
        val habit =
            Habit(
                habitId = 1L,
                name = "Test Habit",
                lastResetAt = lastResetAt,
            )
        val streak = mockk<Streak>()
        return HabitWithStreak(habit, streak)
    }

    @Test
    fun `formattedDurationSinceReset returns 3 decades`() {
        val thirtyYears = 30 * 365 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(thirtyYears)
        assertEquals("3 decades", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 decade`() {
        val tenYears = 10 * 365 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(tenYears)
        assertEquals("1 decade", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 3 years`() {
        val threeYears = 3 * 365 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(threeYears)
        assertEquals("3 years", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 months`() {
        val fiveMonths = 5 * 30 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveMonths)
        assertEquals("5 months", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 month`() {
        val oneMonth = 30 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneMonth)
        assertEquals("1 month", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 year & 2 mos`() {
        val oneYearTwoMonths = (365 + 60) * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneYearTwoMonths)
        assertEquals("1 year & 2 mos", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 months & 2 wks`() {
        val fiveMonthsTwoWeeks = (5 * 30 + 14) * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveMonthsTwoWeeks)
        assertEquals("5 months & 2 wks", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 2 weeks & 1 day`() {
        val twoWeeksOneDay = (2 * 7 + 1) * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(twoWeeksOneDay)
        assertEquals("2 weeks & 1 day", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 2 weeks`() {
        val twoWeeks = 2 * 7 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(twoWeeks)
        assertEquals("2 weeks", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 days`() {
        val fiveDays = 5 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveDays)
        assertEquals("5 days", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 days & 2 hrs`() {
        val fiveDaysTwoHours = (5 * 24 + 2) * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveDaysTwoHours)
        assertEquals("5 days & 2 hrs", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 hours`() {
        val fiveHours = 5 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveHours)
        assertEquals("5 hours", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 hours & 2 mins`() {
        val fiveHoursTwoMinutes = (5 * 60 + 2) * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveHoursTwoMinutes)
        assertEquals("5 hours & 2 mins", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 mins & 30 secs`() {
        val fiveMinutesThirtySeconds = (5 * 60 + 30) * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveMinutesThirtySeconds)
        assertEquals("5 mins & 30 secs", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 mins & 1 sec`() {
        val fiveMinutesOneSecond = (5 * 60 + 1) * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveMinutesOneSecond)
        assertEquals("5 mins & 1 sec", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 5 minutes`() {
        val fiveMinutes = 5 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(fiveMinutes)
        assertEquals("5 minutes", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 3 seconds`() {
        val threeSeconds = 3 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(threeSeconds)
        assertEquals("3 seconds", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 second`() {
        val oneSecond = 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneSecond)
        assertEquals("1 second", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 minute`() {
        val oneMinute = 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneMinute)
        assertEquals("1 minute", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 hour`() {
        val oneHour = 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneHour)
        assertEquals("1 hour", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 day`() {
        val oneDay = 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneDay)
        assertEquals("1 day", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 week`() {
        val oneWeek = 7 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneWeek)
        assertEquals("1 week", habitWithStreak.formattedDurationSinceReset)
    }

    @Test
    fun `formattedDurationSinceReset returns 1 year`() {
        val oneYear = 365 * 24 * 60 * 60 * 1000L
        val habitWithStreak = createHabitWithLastResetAt(oneYear)
        assertEquals("1 year", habitWithStreak.formattedDurationSinceReset)
    }
}
