package io.track.habit.domain.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun Date.formatActiveSinceDate(): String {
    val format = "M/d/yyyy hh:mm a"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val formatter = DateTimeFormatter.ofPattern(format)

        val localDateTime =
            toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

        return localDateTime.format(formatter)
    } else {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(this)
    }
}

fun formatDateDuration(
    startDate: Date,
    endDate: Date,
): String {
    val format = "M/dd/yy"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val formatter = DateTimeFormatter.ofPattern(format)

        val startDateTime =
            startDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        val endDateTime =
            endDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

        return "${startDateTime.format(formatter)} - ${endDateTime.format(formatter)}"
    } else {
        val dateFormat = SimpleDateFormat("M/dd/yy", Locale.getDefault())

        return "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
    }
}
