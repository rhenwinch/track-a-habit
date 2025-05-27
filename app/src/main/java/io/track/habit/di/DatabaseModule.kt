package io.track.habit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.track.habit.data.local.database.AppDatabase
import io.track.habit.data.local.database.AppDatabase.Companion.APP_DATABASE_NAME
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.dao.HabitLogDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return AppDatabase.create(context)
    }

    @Provides
    fun provideHabitDao(appDatabase: AppDatabase): HabitDao {
        return appDatabase.habitDao()
    }

    @Provides
    fun provideHabitLogDao(appDatabase: AppDatabase): HabitLogDao {
        return appDatabase.habitLogDao()
    }
}
