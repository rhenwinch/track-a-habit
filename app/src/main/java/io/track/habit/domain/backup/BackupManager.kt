package io.track.habit.domain.backup

import io.track.habit.domain.model.BackupFile
import java.io.File

/**
 * Interface defining operations for backing up and restoring database content.
 * This interface generalizes the operations to support both local and remote backup strategies.
 */
interface BackupManager {
    /**
     * Creates a backup of the database.
     *
     * @param destination The destination where the backup should be stored.
     * For local implementations, this would be a file path.
     * For remote implementations, this could be a remote URI or identifier.
     * @return Result object indicating success or failure with error details.
     */
    suspend fun createBackup(destination: String = ""): Result<File>

    /**
     * Restores the database from a backup.
     *
     * @param source The source from which to restore the database.
     * For local implementations, this would be a file path.
     * For remote implementations, this could be a remote URI or identifier.
     * @return Result object indicating success or failure with error details.
     */
    suspend fun restoreFromBackup(source: String): Result<Unit>

    /**
     * Lists available backups.
     *
     * @param directory The directory to search for backups (for local implementations).
     * For remote implementations, this could be a remote path or empty.
     * @return Result containing a list of available backups or error details.
     */
    suspend fun listAvailableBackups(directory: String = ""): Result<List<BackupFile>>

    /**
     * Deletes a specific backup.
     *
     * @param backupPath Path to the backup to delete.
     * @return Result indicating success or failure with error details.
     */
    suspend fun deleteBackup(backupPath: String): Result<Unit>
}
