package io.track.habit.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.track.habit.data.local.assets.AndroidAssetReader
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.dao.HabitLogDao
import io.track.habit.data.repository.HabitLogsRepositoryImpl
import io.track.habit.data.repository.HabitRepositoryImpl
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideHabitRepository(habitDao: HabitDao): HabitRepository {
        return HabitRepositoryImpl(habitDao)
    }

    @Singleton
    @Provides
    fun provideHabitLogsRepository(habitLogsDao: HabitLogDao): HabitLogsRepository {
        return HabitLogsRepositoryImpl(habitLogsDao)
    }

    @Singleton
    @Provides
    fun provideAndroidAssetReader(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): AssetReader {
        return AndroidAssetReader(context, ioDispatcher)
    }
}
