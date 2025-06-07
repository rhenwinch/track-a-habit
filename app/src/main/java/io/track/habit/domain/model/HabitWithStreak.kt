package io.track.habit.domain.model

import android.os.Build
import androidx.compose.runtime.Immutable
import io.track.habit.data.local.database.entities.Habit
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Represents a data model that combines a Habit and its associated streak information.
 * This model is immutable and provides utility properties for formatted date and duration.
 *
 * @property habit The habit entity containing details like name and last reset timestamp.
 * @property streak The streak associated with the habit.
 */
@Immutable
data class HabitWithStreak(
    val habit: Habit,
    val streak: Streak,
) {
    /**
     * Formats the `lastResetAt` date of the habit into a human-readable string.
     * Uses `DateTimeFormatter` for API >= 26 and `SimpleDateFormat` for older versions.
     *
     * @return A formatted string representing the date and time of the last reset.
     */
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

    /**
     * Calculates the duration since the habit was last reset and formats it into a human-readable string.
     * The duration is expressed in terms of decades, years, months, weeks, days, hours, minutes, or seconds.
     *
     * @return A formatted string representing the duration since the last reset.
     */
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

    fun getName(isCensored: Boolean): String {
        return if (isCensored) {
            val censoredName =
                when {
                    habit.name.length <= 2 -> {
                        buildString {
                            append(habit.name)
                            while (length < 3) {
                                val lastChar = lastOrNull() ?: 'A'
                                val shiftedChar =
                                    when {
                                        lastChar.isLetter() -> {
                                            val base = if (lastChar.isUpperCase()) 'A' else 'a'
                                            ((lastChar.code - base.code + 3) % 26 + base.code).toChar()
                                        }

                                        else -> 'A'
                                    }

                                append(shiftedChar)
                            }
                        }.take(2)
                    }

                    else -> habit.name.take(2)
                }
            val paddedName = censoredName.padEnd(8, '*')

            paddedName
        } else {
            habit.name
        }
    }
}
