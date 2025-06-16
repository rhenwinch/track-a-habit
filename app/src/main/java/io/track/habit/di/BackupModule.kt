package io.track.habit.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.track.habit.data.remote.backup.RemoteBackupManager
import io.track.habit.data.remote.drive.GoogleDriveService
import io.track.habit.domain.backup.BackupManager
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    @Provides
    @Singleton
    fun provideGoogleDriveService(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): GoogleDriveService {
        return GoogleDriveService(context, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        driveService: GoogleDriveService,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): BackupManager {
        return RemoteBackupManager(context, driveService, ioDispatcher)
    }
}
