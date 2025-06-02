package io.track.habit.domain.datastore

import androidx.datastore.preferences.core.Preferences

interface SettingEntity {
    fun toPreferencesMap(): Map<Preferences.Key<*>, Any>
}
