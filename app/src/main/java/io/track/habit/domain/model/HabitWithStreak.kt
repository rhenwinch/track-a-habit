package io.track.habit.domain.model

import android.content.Context
import androidx.compose.runtime.Stable
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.utils.formatActiveSinceDate
import java.util.Date

/**
 * Represents a data model that combines a Habit and its associated streak information.
 * This model is immutable and provides utility properties for formatted date and duration.
 *
 * @property habit The habit entity containing details like name and last reset timestamp.
 * @property streak The streak associated with the habit.
 */
@Stable
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
        get() = habit.lastResetAt.formatActiveSinceDate()

    companion object {
        fun HabitWithStreak.getFormattedDurationSinceReset(context: Context): String {
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
                    if (decades == 1) {
                        context.getString(R.string.duration_decade_single)
                    } else {
                        context.getString(R.string.duration_decade_plural, decades)
                    }
                }

                totalYears >= 1 -> {
                    val years = totalYears.toInt()
                    val remainingDays = (totalDays - years * 365).toInt()
                    val remainingMonths = remainingDays / 30

                    when {
                        remainingMonths >= 1 -> {
                            val yearText = if (years == 1) {
                                context.getString(R.string.duration_year_single)
                            } else {
                                context.getString(R.string.duration_year_plural, years)
                            }
                            val monthText = if (remainingMonths == 1) {
                                context.getString(R.string.duration_month_abbr_single)
                            } else {
                                context.getString(R.string.duration_month_abbr, remainingMonths)
                            }

                            context.getString(R.string.duration_year_month, yearText, monthText)
                        }

                        years == 1 -> context.getString(R.string.duration_year_single)
                        else -> context.getString(R.string.duration_year_plural, years)
                    }
                }

                totalMonths >= 1 -> {
                    val months = totalMonths.toInt()
                    val remainingDays = (totalDays - months * 30).toInt()
                    val remainingWeeks = remainingDays / 7

                    when {
                        remainingWeeks >= 1 -> {
                            val monthText = if (months == 1) {
                                context.getString(R.string.duration_month_single)
                            } else {
                                context.getString(R.string.duration_month_plural, months)
                            }
                            val weekText = if (remainingWeeks == 1) {
                                context.getString(R.string.duration_week_abbr_single)
                            } else {
                                context.getString(R.string.duration_week_abbr, remainingWeeks)
                            }
                            context.getString(R.string.duration_month_week, monthText, weekText)
                        }

                        months == 1 -> context.getString(R.string.duration_month_single)
                        else -> context.getString(R.string.duration_month_plural, months)
                    }
                }

                totalWeeks >= 1 -> {
                    val weeks = totalWeeks.toInt()
                    val remainingDays = (totalDays - weeks * 7).toInt()

                    when {
                        remainingDays >= 1 -> {
                            val weekText = if (weeks == 1) {
                                context.getString(R.string.duration_week_single)
                            } else {
                                context.getString(R.string.duration_week_plural, weeks)
                            }
                            val dayText = if (remainingDays == 1) {
                                context.getString(R.string.duration_day_abbr_single)
                            } else {
                                context.getString(R.string.duration_day_abbr, remainingDays)
                            }

                            context.getString(R.string.duration_week_day, weekText, dayText)
                        }

                        weeks == 1 -> context.getString(R.string.duration_week_single)
                        else -> context.getString(R.string.duration_week_plural, weeks)
                    }
                }

                totalDays >= 1 -> {
                    val days = totalDays.toInt()
                    val remainingHours = (totalHours - days * 24).toInt()

                    when {
                        remainingHours >= 1 -> {
                            val dayText = if (days == 1) {
                                context.getString(R.string.duration_day_single)
                            } else {
                                context.getString(R.string.duration_day_plural, days)
                            }
                            val hourText = if (remainingHours == 1) {
                                context.getString(R.string.duration_hour_abbr_single)
                            } else {
                                context.getString(R.string.duration_hour_abbr, remainingHours)
                            }
                            context.getString(R.string.duration_day_hour, dayText, hourText)
                        }

                        days == 1 -> context.getString(R.string.duration_day_single)
                        else -> context.getString(R.string.duration_day_plural, days)
                    }
                }

                totalHours >= 1 -> {
                    val hours = totalHours.toInt()
                    if (hours == 1) {
                        context.getString(R.string.duration_hour_single)
                    } else {
                        context.getString(R.string.duration_hour_plural, hours)
                    }
                }

                // For minutes and seconds, return "A while ago"
                else -> context.getString(R.string.duration_a_while_ago)
            }
        }
    }
}
