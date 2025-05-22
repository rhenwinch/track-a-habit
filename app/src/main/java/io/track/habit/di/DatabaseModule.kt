package io.track.habit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.track.habit.data.database.AppDatabase
import io.track.habit.data.database.AppDatabase.Companion.APP_DATABASE_NAME
import io.track.habit.data.database.dao.HabitDao
import io.track.habit.data.database.dao.HabitLogDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
    ): AppDatabase {
        return Room
            .databaseBuilder(
                appContext,
                AppDatabase::class.java,
                APP_DATABASE_NAME,
            ).build()
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
