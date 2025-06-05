package io.track.habit.domain.model

import android.os.Build
import androidx.compose.runtime.Immutable
import io.track.habit.data.local.database.entities.Habit
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Immutable
data class HabitWithStreak(
    val habit: Habit,
    val streak: Streak,
) {
    val formattedActiveSinceDate: String
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

    val formattedDurationSinceReset: String
        get() {
            val currentTime = Date().time
            val resetTime = habit.lastResetAt.time
            val diffMillis = currentTime - resetTime

            val totalSeconds = diffMillis / 1000
            val totalMinutes = totalSeconds / 60
            val totalHours = totalMinutes / 60
            val totalDays = totalHours / 24
            val totalWeeks = totalDays / 7
            val totalMonths = totalDays / 30 // Approximate
            val totalYears = totalDays / 365 // Approximate
            val totalDecades = totalYears / 10

            return when {
                totalDecades >= 1 -> {
                    val decades = totalDecades.toInt()
                    if (decades == 1) "1 decade" else "$decades decades"
                }
                totalYears >= 1 -> {
                    val years = totalYears.toInt()
                    val remainingDays = (totalDays - years * 365).toInt()
                    val remainingMonths = remainingDays / 30

                    when {
                        remainingMonths >= 1 -> {
                            val yearText = if (years == 1) "1 year" else "$years years"
                            val monthText = if (remainingMonths == 1) "1 mo" else "$remainingMonths mos"
                            "$yearText & $monthText"
                        }
                        years == 1 -> "1 year"
                        else -> "$years years"
                    }
                }
                totalMonths >= 1 -> {
                    val months = totalMonths.toInt()
                    val remainingDays = (totalDays - months * 30).toInt()
                    val remainingWeeks = remainingDays / 7

                    when {
                        remainingWeeks >= 1 -> {
                            val monthText = if (months == 1) "1 month" else "$months months"
                            val weekText = if (remainingWeeks == 1) "1 wk" else "$remainingWeeks wks"
                            "$monthText & $weekText"
                        }
                        months == 1 -> "1 month"
                        else -> "$months months"
                    }
                }
                totalWeeks >= 1 -> {
                    val weeks = totalWeeks.toInt()
                    val remainingDays = (totalDays - weeks * 7).toInt()

                    when {
                        remainingDays >= 1 -> {
                            val weekText = if (weeks == 1) "1 week" else "$weeks weeks"
                            val dayText = if (remainingDays == 1) "1 day" else "$remainingDays days"
                            "$weekText & $dayText"
                        }
                        weeks == 1 -> "1 week"
                        else -> "$weeks weeks"
                    }
                }
                totalDays >= 1 -> {
                    val days = totalDays.toInt()
                    val remainingHours = (totalHours - days * 24).toInt()

                    when {
                        remainingHours >= 1 -> {
                            val dayText = if (days == 1) "1 day" else "$days days"
                            val hourText = if (remainingHours == 1) "1 hr" else "$remainingHours hrs"
                            "$dayText & $hourText"
                        }
                        days == 1 -> "1 day"
                        else -> "$days days"
                    }
                }
                totalHours >= 1 -> {
                    val hours = totalHours.toInt()
                    val remainingMinutes = (totalMinutes - hours * 60).toInt()

                    when {
                        remainingMinutes >= 1 -> {
                            val hourText = if (hours == 1) "1 hour" else "$hours hours"
                            val minuteText = if (remainingMinutes == 1) "1 min" else "$remainingMinutes mins"
                            "$hourText & $minuteText"
                        }
                        hours == 1 -> "1 hour"
                        else -> "$hours hours"
                    }
                }
                totalMinutes >= 1 -> {
                    val minutes = totalMinutes.toInt()
                    val remainingSeconds = (totalSeconds - minutes * 60).toInt()

                    when {
                        remainingSeconds >= 1 -> {
                            val minuteText = if (minutes == 1) "1 min" else "$minutes mins"
                            val secondText = if (remainingSeconds == 1) "1 sec" else "$remainingSeconds secs"
                            "$minuteText & $secondText"
                        }
                        minutes == 1 -> "1 minute"
                        else -> "$minutes minutes"
                    }
                }
                totalSeconds >= 1 -> {
                    val seconds = totalSeconds.toInt()
                    if (seconds == 1) "1 second" else "$seconds seconds"
                }
                else -> "0 seconds"
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
