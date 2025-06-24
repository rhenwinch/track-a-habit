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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Implementation of [BackupManager] that uses Google Drive for remote backups.
 *
 * This class handles backup and restore operations using Google Drive as storage.
 * Uses ZIP compression to bundle all SQLite files (main DB, WAL, and SHM files)
 * to ensure complete and consistent backup and restore.
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
            const val BACKUP_FILE_PREFIX = "backup_"
            const val BACKUP_FILE_EXTENSION = ".zip"
            private const val BACKUP_MIME_TYPE = "application/zip"

            private const val WAL_EXTENSION = "-wal"
            private const val SHM_EXTENSION = "-shm"
            private const val BUFFER_SIZE = 8192
        }

        /**
         * Creates a backup of the database in Google Drive.
         * Backs up all SQLite files (main DB, WAL, and SHM) in a single ZIP archive.
         *
         * @param destination Optional custom folder name in Google Drive
         * @return Result containing the temporary local file used for backup
         */
        override suspend fun createBackup(destination: String): Result<File> =
            withContext(dispatcher) {
                try {
                    lock.read {
                        if (!driveService.isAuthenticated()) {
                            return@withContext Result.failure(
                                IllegalStateException("User is not signed in to Google account"),
                            )
                        }

                        val dbFile = context.getDatabasePath(AppDatabase.APP_DATABASE_NAME)
                        if (!dbFile.exists()) {
                            return@withContext Result.failure(
                                IllegalStateException("Database file does not exist"),
                            )
                        }

                        val walFile = File(dbFile.path + WAL_EXTENSION)
                        val shmFile = File(dbFile.path + SHM_EXTENSION)

                        val timestamp = dateFormat.format(Date())
                        val tempZipFile = File(
                            context.cacheDir,
                            "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION",
                        )

                        createZipArchive(tempZipFile, dbFile, walFile, shmFile)

                        val folderName = destination.ifEmpty { BACKUP_FOLDER_NAME }
                        val folderId = driveService.findOrCreateFolder(folderName)

                        val fileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
                        driveService.uploadFile(
                            file = tempZipFile,
                            fileName = fileName,
                            mimeType = BACKUP_MIME_TYPE,
                            folderId = folderId,
                        )

                        return@withContext Result.success(tempZipFile)
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Restores the database from a Google Drive backup file.
         * Extracts all SQLite files (main DB, WAL, and SHM) from the ZIP archive.
         * Forces Room to reconnect to the database after restore.
         *
         * @param source The file ID of the Google Drive backup file
         * @return Result indicating success or failure
         */
        override suspend fun restoreFromBackup(source: String): Result<Unit> =
            withContext(dispatcher) {
                try {
                    lock.write {
                        if (!driveService.isAuthenticated()) {
                            return@withContext Result.failure(
                                IllegalStateException("User is not signed in to Google account"),
                            )
                        }

                        val outputStream = ByteArrayOutputStream()
                        driveService.downloadFile(fileId = source, outputStream = outputStream)

                        AppDatabase.closeDatabase()

                        val tempZipFile = File(context.cacheDir, "temp_restore_${UUID.randomUUID()}.zip")
                        FileOutputStream(tempZipFile).use { fileOutputStream ->
                            outputStream.writeTo(fileOutputStream)
                        }

                        val dbFile = context.getDatabasePath(AppDatabase.APP_DATABASE_NAME)
                        val walFile = File(dbFile.path + WAL_EXTENSION)
                        val shmFile = File(dbFile.path + SHM_EXTENSION)

                        if (dbFile.exists()) dbFile.delete()
                        if (walFile.exists()) walFile.delete()
                        if (shmFile.exists()) shmFile.delete()

                        extractZipArchive(tempZipFile, dbFile.parentFile!!)

                        tempZipFile.delete()

                        return@withContext Result.success(Unit)
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Creates a ZIP archive containing the database files.
         *
         * @param zipFile Output ZIP file
         * @param dbFile Main database file
         * @param walFile Write-ahead log file
         * @param shmFile Shared memory file
         */
        private fun createZipArchive(
            zipFile: File,
            dbFile: File,
            walFile: File,
            shmFile: File,
        ) {
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                addFileToZip(zipOut, dbFile, dbFile.name)

                if (walFile.exists()) {
                    addFileToZip(zipOut, walFile, walFile.name)
                }

                if (shmFile.exists()) {
                    addFileToZip(zipOut, shmFile, shmFile.name)
                }
            }
        }

        /**
         * Adds a single file to a ZIP archive.
         *
         * @param zipOut ZIP output stream
         * @param file File to add
         * @param entryName Name of the entry in the ZIP
         */
        private fun addFileToZip(
            zipOut: ZipOutputStream,
            file: File,
            entryName: String,
        ) {
            if (!file.exists()) return

            FileInputStream(file).use { fileIn ->
                val zipEntry = ZipEntry(entryName)
                zipOut.putNextEntry(zipEntry)

                val buffer = ByteArray(BUFFER_SIZE)
                var length: Int
                while (fileIn.read(buffer).also { length = it } > 0) {
                    zipOut.write(buffer, 0, length)
                }
                zipOut.closeEntry()
            }
        }

        /**
         * Extracts files from a ZIP archive.
         *
         * @param zipFile ZIP file to extract
         * @param destDir Destination directory
         * @throws IOException if there's an error during extraction
         */
        private fun extractZipArchive(
            zipFile: File,
            destDir: File,
        ) {
            if (!destDir.exists()) {
                destDir.mkdirs()
            }

            ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
                var zipEntry = zipIn.nextEntry
                val buffer = ByteArray(BUFFER_SIZE)

                while (zipEntry != null) {
                    val newFile = File(destDir, zipEntry.name)

                    if (zipEntry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        val parent = newFile.parentFile
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs()
                        }

                        FileOutputStream(newFile).use { fileOut ->
                            var len: Int
                            while (zipIn.read(buffer).also { len = it } > 0) {
                                fileOut.write(buffer, 0, len)
                            }
                        }
                    }
                    zipIn.closeEntry()
                    zipEntry = zipIn.nextEntry
                }
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
                    if (!driveService.isAuthenticated()) {
                        return@withContext Result.failure(
                            IllegalStateException("User is not signed in to Google account"),
                        )
                    }

                    val folderName = directory.ifEmpty { BACKUP_FOLDER_NAME }
                    val folderId = driveService.findOrCreateFolder(folderName)

                    val driveFiles = driveService.listFiles(folderId, BACKUP_MIME_TYPE)

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
                    if (!driveService.isAuthenticated()) {
                        return@withContext Result.failure(
                            IllegalStateException("User is not signed in to Google account"),
                        )
                    }

                    driveService.deleteFile(backupPath)

                    return@withContext Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
