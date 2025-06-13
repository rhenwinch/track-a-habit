package io.track.habit.data.local.datastore.entities

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.utils.stringLiteral

data class UserAppState(
    val lastShowcasedHabitId: Long = UserAppStateRegistry.LAST_SHOWCASED_HABIT_ID.defaultValue,
    val isFirstRun: Boolean = UserAppStateRegistry.IS_FIRST_RUN.defaultValue,
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
            )
        }
    }

    override fun toPreferencesMap(): Map<Preferences.Key<*>, Any> {
        return mapOf(
            Pair(intPreferencesKey(UserAppStateRegistry.LAST_SHOWCASED_HABIT_ID.key), lastShowcasedHabitId),
            Pair(booleanPreferencesKey(UserAppStateRegistry.IS_FIRST_RUN.key), isFirstRun),
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
}
