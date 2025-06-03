package io.track.habit.domain.model

import android.os.Build
import io.track.habit.data.local.database.entities.Habit
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HabitWithStreak(
    val habit: Habit,
    val streak: Streak,
) {
    val formattedDate: String
        get() {
            val format = "M/d/yyyy hh:mm a"
            val date = habit.lastResetAt

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val formatter = DateTimeFormatter.ofPattern(format)

                val localDateTime =
                    date
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                return localDateTime.format(formatter)
            } else {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                return formatter.format(date)
            }
        }

    companion object {
        private fun randomLetter(): Char = ('A'..'z').random()

        fun HabitWithStreak.censorName(isCensored: Boolean): HabitWithStreak {
            val name = habit.name

            return if (isCensored) {
                val censoredName =
                    when {
                        name.length <= 2 -> {
                            buildString {
                                append(name)
                                while (length < 3) {
                                    append(randomLetter())
                                }
                            }.take(2)
                        }

                        else -> name.take(2)
                    }
                val paddedName = censoredName.padEnd(8, '*')

                copy(habit = habit.copy(name = paddedName))
            } else {
                this
            }
        }
    }
}
