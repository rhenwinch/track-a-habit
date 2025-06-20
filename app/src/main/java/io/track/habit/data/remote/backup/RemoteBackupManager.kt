package io.track.habit.data.remote.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.track.habit.data.local.database.AppDatabase
import io.track.habit.data.remote.drive.GoogleDriveService
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.backup.BackupManager
import io.track.habit.domain.model.BackupFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Implementation of [BackupManager] that uses Google Drive for remote backups.
 *
 * This class handles backup and restore operations using Google Drive as storage.
 *
 * @param context Application context used to access database files
 * @param driveService Service for Google Drive operations
 * @param dispatcher IO dispatcher for performing backup operations
 */
@Singleton
class RemoteBackupManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val driveService: GoogleDriveService,
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
    ) : BackupManager {
        private val lock = ReentrantReadWriteLock()
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

        companion object {
            private const val BACKUP_FOLDER_NAME = "Track a Habit-Backups"
            private const val BACKUP_FILE_PREFIX = "backup_"
            private const val BACKUP_FILE_EXTENSION = ".db"
            private const val BACKUP_MIME_TYPE = "application/octet-stream"
        }

        /**
         * Creates a backup of the database in Google Drive.
         *
         * @param destination Optional custom folder name in Google Drive
         * @return Result containing the temporary local file used for backup
         */
        override suspend fun createBackup(destination: String): Result<File> =
            withContext(dispatcher) {
                try {
                    lock.read {
                        // Ensure user is signed in
                        if (!driveService.isAuthenticated()) {
                            return@withContext Result.failure(
                                IllegalStateException("User is not signed in to Google account"),
                            )
                        }

                        // Get source database file
                        val dbFile = context.getDatabasePath(AppDatabase.APP_DATABASE_NAME)
                        if (!dbFile.exists()) {
                            return@withContext Result.failure(
                                IllegalStateException("Database file does not exist"),
                            )
                        }

                        // Create temporary file for backup
                        val timestamp = dateFormat.format(Date())
                        val tempFile = File(
                            context.cacheDir,
                            "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION",
                        )

                        // Copy database to temp file
                        FileInputStream(dbFile).channel.use { sourceChannel ->
                            FileOutputStream(tempFile).channel.use { destChannel ->
                                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
                            }
                        }

                        // Find or create backup folder in Drive
                        val folderName = destination.ifEmpty { BACKUP_FOLDER_NAME }
                        val folderId = driveService.findOrCreateFolder(folderName)

                        // Upload file to Drive
                        val fileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
                        driveService.uploadFile(
                            file = tempFile,
                            fileName = fileName,
                            mimeType = BACKUP_MIME_TYPE,
                            folderId = folderId,
                        )

                        return@withContext Result.success(tempFile)
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Restores the database from a Google Drive backup file.
         *
         * @param source The file ID of the Google Drive backup file
         * @return Result indicating success or failure
         */
        override suspend fun restoreFromBackup(source: String): Result<Unit> =
            withContext(dispatcher) {
                try {
                    lock.write {
                        // Ensure user is signed in
                        if (!driveService.isAuthenticated()) {
                            return@withContext Result.failure(
                                IllegalStateException("User is not signed in to Google account"),
                            )
                        }

                        // Retrieve file from Drive
                        val outputStream = ByteArrayOutputStream()
                        driveService.downloadFile(fileId = source, outputStream = outputStream)

                        // Close the database to ensure it's not in use
                        AppDatabase.closeDatabase()

                        // Get destination database file
                        val dbFile = context.getDatabasePath(AppDatabase.APP_DATABASE_NAME)

                        // Write backup data to database file
                        FileOutputStream(dbFile).use { fileOutputStream ->
                            outputStream.writeTo(fileOutputStream)
                        }

                        return@withContext Result.success(Unit)
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Lists all available backups in Google Drive.
         *
         * @param directory Optional custom folder name in Google Drive
         * @return Result containing a list of file metadata for available backups
         */
        override suspend fun listAvailableBackups(directory: String): Result<List<BackupFile>> =
            withContext(dispatcher) {
                try {
                    // Ensure user is signed in
                    if (!driveService.isAuthenticated()) {
                        return@withContext Result.failure(
                            IllegalStateException("User is not signed in to Google account"),
                        )
                    }

                    // Find the folder
                    val folderName = directory.ifEmpty { BACKUP_FOLDER_NAME }
                    val folderId = driveService.findOrCreateFolder(folderName)

                    // List files in the folder
                    val driveFiles = driveService.listFiles(folderId, BACKUP_MIME_TYPE)

                    // Convert Drive files to local File representations
                    val fileList = driveFiles.map { driveFile ->
                        BackupFile(
                            id = driveFile.id,
                            name = driveFile.name,
                        )
                    }

                    return@withContext Result.success(fileList)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Deletes a specific backup file from Google Drive.
         *
         * @param backupPath The file ID of the Google Drive backup file
         * @return Result indicating success or failure
         */
        override suspend fun deleteBackup(backupPath: String): Result<Unit> =
            withContext(dispatcher) {
                try {
                    // Ensure user is signed in
                    if (!driveService.isAuthenticated()) {
                        return@withContext Result.failure(
                            IllegalStateException("User is not signed in to Google account"),
                        )
                    }

                    // Extract the Drive file ID from the path
                    val fileId = if (backupPath.contains("::")) {
                        backupPath.split("::")[0]
                    } else {
                        backupPath
                    }

                    // Delete the file
                    driveService.deleteFile(fileId)

                    return@withContext Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
