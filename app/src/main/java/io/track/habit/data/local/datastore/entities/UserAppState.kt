package io.track.habit.data.local.datastore.entities

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.utils.stringLiteral

data class UserAppState(
    val lastShowcasedHabitId: Long = UserAppStateRegistry.LAST_SHOWCASED_HABIT_ID.defaultValue,
    val isFirstRun: Boolean = UserAppStateRegistry.IS_FIRST_RUN.defaultValue,
    val lastNotifiedStreaks: String = UserAppStateRegistry.LAST_NOTIFIED_STREAKS.defaultValue,
) : SettingEntity {
    companion object {
        fun fromPreferences(preferences: Preferences): UserAppState {
            return UserAppState(
                lastShowcasedHabitId =
                    preferences[longPreferencesKey(UserAppStateRegistry.LAST_SHOWCASED_HABIT_ID.key)]
                        ?: UserAppStateRegistry.LAST_SHOWCASED_HABIT_ID.defaultValue,
                isFirstRun =
                    preferences[booleanPreferencesKey(UserAppStateRegistry.IS_FIRST_RUN.key)]
                        ?: UserAppStateRegistry.IS_FIRST_RUN.defaultValue,
                lastNotifiedStreaks =
                    preferences[stringPreferencesKey(UserAppStateRegistry.LAST_NOTIFIED_STREAKS.key)]
                        ?: UserAppStateRegistry.LAST_NOTIFIED_STREAKS.defaultValue,
            )
        }
    }

    override fun toPreferencesMap(): Map<Preferences.Key<*>, Any> {
        return mapOf(
            Pair(intPreferencesKey(UserAppStateRegistry.LAST_SHOWCASED_HABIT_ID.key), lastShowcasedHabitId),
            Pair(booleanPreferencesKey(UserAppStateRegistry.IS_FIRST_RUN.key), isFirstRun),
            Pair(stringPreferencesKey(UserAppStateRegistry.LAST_NOTIFIED_STREAKS.key), lastNotifiedStreaks),
        )
    }
}

object UserAppStateRegistry {
    val LAST_SHOWCASED_HABIT_ID =
        SettingDefinition(
            key = "last_showcased_habit_id",
            defaultValue = 0,
            type = SettingType.LongType,
            displayName = stringLiteral(""),
            description = null,
        )

    val IS_FIRST_RUN =
        SettingDefinition(
            key = "is_first_run",
            defaultValue = true,
            type = SettingType.BooleanType,
            displayName = stringLiteral(""),
            description = null,
        )

    val LAST_NOTIFIED_STREAKS =
        SettingDefinition(
            key = "last_notified_streaks",
            defaultValue = "[]",
            type = SettingType.StringType,
            displayName = stringLiteral(""),
            description = null,
        )
}
