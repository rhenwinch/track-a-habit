package io.track.habit.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.track.habit.data.local.database.converter.DateConverters
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.dao.HabitLogDao
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 1,
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    abstract fun habitLogDao(): HabitLogDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        const val APP_DATABASE_NAME = "app_database"

        fun create(context: Context): AppDatabase {
            return INSTANCE ?: Room
                .databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    APP_DATABASE_NAME,
                ).build()
                .also { db ->
                    INSTANCE = db
                }
        }

        /**
         * Closes the database instance if it's open.
         * This is necessary for backup and restore operations to ensure
         * the database isn't in use during these critical operations.
         */
        fun closeDatabase() {
            INSTANCE?.let { db ->
                if (db.isOpen) {
                    db.close()
                }
                INSTANCE = null
            }
        }
    }
}
