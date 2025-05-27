package io.track.habit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.repository.HabitRepositoryImpl
import io.track.habit.domain.repository.HabitRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideHabitRepository(habitDao: HabitDao): HabitRepository {
        return HabitRepositoryImpl(habitDao)
    }
}
