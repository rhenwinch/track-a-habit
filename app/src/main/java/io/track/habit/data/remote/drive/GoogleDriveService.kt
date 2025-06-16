package io.track.habit.data.remote.drive

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import io.track.habit.BuildConfig
import io.track.habit.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.util.Collections
import javax.inject.Inject
import com.google.api.services.drive.model.File as DriveFile

/**
 * Service for interacting with Google Drive API.
 * Handles authentication, file operations, and folder management.
 */
class GoogleDriveService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
    ) {
        private val credentialManager = CredentialManager.create(context)
        private var currentIdToken: String? = null

        companion object {
            private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
            private const val APP_NAME = "Track-A-Habit Backup"
        }

        /**
         * Gets a Google Drive service instance if the user is authenticated.
         *
         * @return Drive service or null if authentication fails
         */
        private suspend fun getDriveService(): Drive? {
            val idToken = getGoogleIdToken() ?: return null

            val accessToken = AccessToken
                .newBuilder()
                .setTokenValue(idToken)
                .build()

            val credentials = GoogleCredentials
                .newBuilder()
                .setAccessToken(accessToken)
                .build()

            val requestInitializer = HttpCredentialsAdapter(credentials)

            return Drive
                .Builder(
                    NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer,
                ).setApplicationName(APP_NAME)
                .build()
        }

        /**
         * Gets a Google ID token using the Credential Manager API.
         *
         * @return ID token or null if authentication fails
         */
        private suspend fun getGoogleIdToken(): String? =
            withContext(dispatcher) {
                if (currentIdToken != null) {
                    return@withContext currentIdToken
                }

                try {
                    val googleIdOption = GetGoogleIdOption
                        .Builder()
                        .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(true)
                        .setAutoSelectEnabled(true)
                        .setNonce(System.currentTimeMillis().toString())
                        .build()

                    val request = GetCredentialRequest
                        .Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val response = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )

                    val credential = response.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            currentIdToken = idToken
                            return@withContext idToken
                        } catch (e: GoogleIdTokenParsingException) {
                            return@withContext null
                        }
                    }
                    return@withContext null
                } catch (e: Exception) {
                    return@withContext null
                }
            }

        /**
         * Checks if the user is authenticated with Google.
         *
         * @return true if authenticated, false otherwise
         */
        suspend fun isAuthenticated(): Boolean =
            withContext(dispatcher) {
                getGoogleIdToken() != null
            }

        /**
         * Ensures the user is authenticated, throws exception if not.
         *
         * @throws IllegalStateException if user is not authenticated
         */
        private suspend fun requireAuthentication() {
            if (!isAuthenticated()) {
                throw IllegalStateException("User is not signed in to Google account")
            }
        }

        /**
         * Finds or creates a folder in Google Drive.
         *
         * @param folderName Name of the folder to find or create
         * @return ID of the folder
         * @throws IllegalStateException if Drive service is unavailable
         */
        suspend fun findOrCreateFolder(folderName: String): String =
            withContext(dispatcher) {
                requireAuthentication()
                val drive = getDriveService() ?: throw IllegalStateException("Drive service is not available")

                // Check if folder exists
                val query = "name = '$folderName' and mimeType = '$FOLDER_MIME_TYPE' and trashed = false"
                val result = drive
                    .files()
                    .list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute()

                val files = result.files

                if (files != null && files.isNotEmpty()) {
                    // Folder exists, return its ID
                    return@withContext files[0].id
                } else {
                    // Create folder
                    val folderMetadata = DriveFile()
                        .setName(folderName)
                        .setMimeType(FOLDER_MIME_TYPE)

                    val folder = drive
                        .files()
                        .create(folderMetadata)
                        .setFields("id")
                        .execute()

                    return@withContext folder.id
                }
            }

        /**
         * Uploads a file to Google Drive.
         *
         * @param file Local file to upload
         * @param fileName Name for the file in Google Drive
         * @param mimeType MIME type of the file
         * @param folderId ID of the parent folder in Google Drive
         * @return ID of the uploaded file
         * @throws IllegalStateException if Drive service is unavailable
         */
        suspend fun uploadFile(
            file: File,
            fileName: String,
            mimeType: String,
            folderId: String,
        ): String =
            withContext(dispatcher) {
                requireAuthentication()
                val drive = getDriveService() ?: throw IllegalStateException("Drive service is not available")

                // Create file metadata
                val fileMetadata = DriveFile()
                    .setName(fileName)
                    .setMimeType(mimeType)
                    .setParents(Collections.singletonList(folderId))

                // Upload file to Drive
                val fileContent = FileContent(mimeType, file)
                val uploadedFile = drive
                    .files()
                    .create(fileMetadata, fileContent)
                    .setFields("id, name, modifiedTime")
                    .execute()

                return@withContext uploadedFile.id
            }

        /**
         * Downloads a file from Google Drive.
         *
         * @param fileId ID of the file to download
         * @param outputStream Stream to write the downloaded file content
         * @throws IllegalStateException if Drive service is unavailable
         */
        suspend fun downloadFile(
            fileId: String,
            outputStream: OutputStream,
        ) = withContext(dispatcher) {
            requireAuthentication()
            val drive = getDriveService() ?: throw IllegalStateException("Drive service is not available")

            drive
                .files()
                .get(fileId)
                .executeMediaAndDownloadTo(outputStream)
        }

        /**
         * Lists files in a folder in Google Drive.
         *
         * @param folderId ID of the folder
         * @param mimeType Filter files by MIME type (optional)
         * @return List of file metadata
         * @throws IllegalStateException if Drive service is unavailable
         */
        suspend fun listFiles(
            folderId: String,
            mimeType: String? = null,
        ): List<DriveFileMetadata> =
            withContext(dispatcher) {
                requireAuthentication()
                val drive = getDriveService() ?: throw IllegalStateException("Drive service is not available")

                val queryBuilder = StringBuilder("'$folderId' in parents and trashed = false")
                if (mimeType != null) {
                    queryBuilder.append(" and mimeType = '$mimeType'")
                }

                val fileResult = drive
                    .files()
                    .list()
                    .setQ(queryBuilder.toString())
                    .setSpaces("drive")
                    .setFields("files(id, name, modifiedTime, size)")
                    .setOrderBy("modifiedTime desc")
                    .execute()

                val files = fileResult.files ?: emptyList()

                return@withContext files.map { driveFile ->
                    DriveFileMetadata(
                        id = driveFile.id,
                        name = driveFile.name,
                        modifiedTime = driveFile.modifiedTime?.value,
                        size = driveFile.getSize()?.toLong(),
                    )
                }
            }

        /**
         * Deletes a file from Google Drive.
         *
         * @param fileId ID of the file to delete
         * @throws IllegalStateException if Drive service is unavailable
         */
        suspend fun deleteFile(fileId: String) =
            withContext(dispatcher) {
                requireAuthentication()
                val drive = getDriveService() ?: throw IllegalStateException("Drive service is not available")

                drive.files().delete(fileId).execute()
            }

        /**
         * Creates a credential request for Google Sign-In that can be used with Credential Manager.
         *
         * @return The credential request
         */
        private fun createGoogleSignInRequest(): GetCredentialRequest {
            val googleIdOption = GetGoogleIdOption
                .Builder()
                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false) // Show all accounts in selector
                .setAutoSelectEnabled(true)
                .build()

            return GetCredentialRequest
                .Builder()
                .addCredentialOption(googleIdOption)
                .build()
        }

        /**
         * Starts the Google Sign-In flow.
         *
         * @return Result indicating success or failure
         */
        suspend fun signIn(): Result<Unit> =
            withContext(dispatcher) {
                try {
                    val request = createGoogleSignInRequest()

                    val response = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )

                    val credential = response.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            currentIdToken = googleIdTokenCredential.idToken
                            return@withContext Result.success(Unit)
                        } catch (e: GoogleIdTokenParsingException) {
                            return@withContext Result.failure(e)
                        }
                    }
                    return@withContext Result.failure(IllegalStateException("Invalid credential type"))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Signs out the current user by clearing the stored token.
         */
        suspend fun signOut() =
            withContext(dispatcher) {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                currentIdToken = null
            }
    }

/**
 * Data class representing metadata for a file in Google Drive.
 */
data class DriveFileMetadata(
    val id: String,
    val name: String,
    val modifiedTime: Long? = null,
    val size: Long? = null,
)
