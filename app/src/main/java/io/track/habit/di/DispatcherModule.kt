package io.track.habit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.track.habit.domain.utils.coroutines.AppDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = AppDispatcher.IO.dispatcher
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher
