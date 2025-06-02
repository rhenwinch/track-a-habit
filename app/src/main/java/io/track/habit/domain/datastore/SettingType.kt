package io.track.habit.domain.datastore

/**
 * Represents the type of a setting.
 *
 * This sealed class is used to define the possible types of settings that can be stored in the DataStore.
 * Each object represents a specific type, such as String, Boolean, Int or Long.
 *
 * @param T The underlying Kotlin type of the setting.
 */
sealed class SettingType<T> {
    object StringType : SettingType<String>()

    object BooleanType : SettingType<Boolean>()

    object IntType : SettingType<Int>()

    object LongType : SettingType<Long>()
}
