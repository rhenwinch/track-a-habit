package io.track.habit.model

import io.mockk.mockk
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.HabitWithStreak.Companion.censorName
import io.track.habit.domain.model.Streak
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class HabitWithStreakTest {
    @Test
    fun `formattedDate returns correct format on SDK 26 and above (inclusive)`() {
        val testDate = GregorianCalendar(2024, Calendar.MARCH, 5, 9, 30).time
        val habit = Habit(name = "Read", lastResetAt = testDate)
        val streak = mockk<Streak>()
        val habitWithStreak = HabitWithStreak(habit, streak)

        val formatted = habitWithStreak.formattedDate
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

            val censored = original.censorName(true)

            println("Actual habit name: $name")
            println("Censored habit name: ${censored.habit.name}")
            println("====")

            name = name.dropLast(1)
        }
    }

    @Test
    fun `getCensoredHabit censors correctly long names`() {
        val habit = Habit(name = "ExerciseDaily")
        val streak = mockk<Streak>()
        val original = HabitWithStreak(habit, streak)

        val censored = original.censorName(true)

        // Take 2 characters and pad to 8
        println("Censored habit name: ${censored.habit.name}")
        assertEquals("Ex******", censored.habit.name)
    }

    @Test
    fun `getCensoredHabit does not censor when censor is false`() {
        val habit = Habit(name = "ExerciseDaily")
        val streak = mockk<Streak>()
        val original = HabitWithStreak(habit, streak)

        val censored = original.censorName(false)

        println("Censored habit name: ${censored.habit.name}")
        assertEquals("ExerciseDaily", censored.habit.name)
    }
}
