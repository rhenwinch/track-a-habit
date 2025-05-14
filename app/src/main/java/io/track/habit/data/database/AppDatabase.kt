package io.track.habit.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.track.habit.data.database.converter.DateConverters
import io.track.habit.data.database.dao.HabitDao
import io.track.habit.data.database.dao.HabitLogDao
import io.track.habit.domain.model.database.Habit
import io.track.habit.domain.model.database.HabitLog

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 1
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "app_database"

        fun create(context: Context): AppDatabase {
            return INSTANCE ?: Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build().also { db ->
                INSTANCE = db
            }
        }
    }
}
