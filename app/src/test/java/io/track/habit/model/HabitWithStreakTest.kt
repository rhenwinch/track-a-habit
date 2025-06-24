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
}
