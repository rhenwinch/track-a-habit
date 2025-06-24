package io.track.habit.data.remote.drive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import io.track.habit.BuildConfig
import io.track.habit.R
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.stringRes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton
import com.google.api.services.drive.model.File as DriveFile

/**
 * Service for interacting with Google Drive API.
 * Handles authentication, file operations, and folder management.
 */
@Singleton
class GoogleDriveService
    @Inject
    constructor(
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
    ) {
        private lateinit var context: Context // Be careful with context usage, it should be initialized before use
        private val credentialManager by lazy { CredentialManager.create(context) }
        private var googleAccountToken: AccessToken? = null

        private val _authorizationState = MutableStateFlow<AuthorizationState>(AuthorizationState.NotAuthorized)
        val authorizationState: StateFlow<AuthorizationState> = _authorizationState.asStateFlow()

        private val _isInitialized = MutableStateFlow(false)
        val isInitialized = _isInitialized.asStateFlow()

        private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
        private lateinit var authClient: AuthorizationClient

        companion object {
            private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
            private const val APP_NAME = "Track-A-Habit Backup"
            private const val TAG = "GoogleDriveService"

            private val DRIVE_SCOPES = listOf(
                DriveScopes.DRIVE_FILE,
                DriveScopes.DRIVE_APPDATA,
            )
        }

        /**
         * Initializes the Google Drive service with the required context and activity result launcher.
         *
         * @param activity The activity context used for authorization
         * @param launcher The ActivityResultLauncher to handle authorization results
         */
        fun initialize(
            activity: Activity,
            launcher: ActivityResultLauncher<IntentSenderRequest>,
        ) {
            context = activity
            activityResultLauncher = launcher
            authClient = Identity.getAuthorizationClient(context)
            _isInitialized.value = true
        }

        /**
         * Gets a Google Drive service instance if the user is authenticated and authorized.
         *
         * @return Drive service or null if authentication fails
         */
        private fun getDriveService(): Drive? {
            return googleAccountToken?.let {
                val credentials = GoogleCredentials.create(it)

                Drive
                    .Builder(
                        NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        HttpCredentialsAdapter(credentials),
                    ).setApplicationName(APP_NAME)
                    .build()
            }
        }

        /**
         * Checks if the user is authenticated with Google.
         * This method will attempt to ensure authentication if possible.
         *
         * @return true if authenticated, false otherwise
         */
        suspend fun isAuthenticated(): Boolean {
            if (googleAccountToken != null && authorizationState.value == AuthorizationState.Authorized) {
                return true
            }

            try {
                signIn()
                    .onSuccess {
                        return true
                    }

                return false
            } catch (e: Exception) {
                Log.d(TAG, "Silent authentication check failed: ${e.message}")
                return false
            }
        }

        /**
         * Ensures the user is authenticated, throws exception if not.
         *
         * @throws IllegalStateException if user is not authenticated
         */
        private suspend fun requireAuthentication() {
            if (!isAuthenticated()) {
                throw IllegalStateException(context.getString(R.string.error_not_signed_in))
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
                val drive =
                    getDriveService()
                        ?: throw IllegalStateException(context.getString(R.string.error_drive_service_unavailable))

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
                    return@withContext files[0].id
                } else {
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
                val drive =
                    getDriveService()
                        ?: throw IllegalStateException(context.getString(R.string.error_drive_service_unavailable))

                val fileMetadata = DriveFile()
                    .setName(fileName)
                    .setMimeType(mimeType)
                    .setParents(Collections.singletonList(folderId))

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
         * Starts the Google Sign-In flow.
         *
         * @return Result indicating success or failure
         */
        suspend fun signIn(filterByAuthorizedAccounts: Boolean = true): Result<Unit> {
            return try {
                val googleIdOption = GetGoogleIdOption
                    .Builder()
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                    .setAutoSelectEnabled(filterByAuthorizedAccounts)
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
                        GoogleIdTokenCredential.createFrom(credential.data)
                        requestGoogleDriveAuthorization()
                        return Result.success(Unit)
                    } catch (e: GoogleIdTokenParsingException) {
                        return Result.failure(e)
                    }
                }

                Result.failure(IllegalStateException("Invalid credential type"))
            } catch (e: Exception) {
                if (e is NoCredentialException && filterByAuthorizedAccounts) {
                    return signIn(filterByAuthorizedAccounts = false)
                }

                Result.failure(e)
            }
        }

        /**
         * Requests authorization for Google Drive access using the Identity Authorization API.
         * This must be called from a context that can handle the authorization resolution.
         */
        private suspend fun requestGoogleDriveAuthorization() {
            _authorizationState.value = AuthorizationState.Authorizing

            val authRequest = AuthorizationRequest
                .builder()
                .setRequestedScopes(DRIVE_SCOPES.map { Scope(it) })
                .build()

            runCatching {
                authClient
                    .authorize(authRequest)
                    .await()
            }.onSuccess { authResult ->
                if (authResult.hasResolution() && authResult.pendingIntent != null) {
                    try {
                        val intentSenderRequest = IntentSenderRequest
                            .Builder(authResult.pendingIntent!!.intentSender)
                            .build()
                        activityResultLauncher.launch(intentSenderRequest)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start Authorization UI: ${e.localizedMessage}")
                        _authorizationState.value =
                            AuthorizationState.Error(stringRes(R.string.error_launch_auth_failed, e.message ?: ""))
                    }
                } else {
                    setupGoogleAccountCredential(authResult)
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to authorize", e)
                _authorizationState.value =
                    AuthorizationState.Error(stringRes(R.string.error_auth_failed_with_reason, e.message ?: ""))
            }
        }

        /**
         * Should be called from the activity that handles the authorization result.
         *
         * @param resultCode The result code from onActivityResult
         * @param data The intent data from onActivityResult
         */
        fun handleAuthorizationResult(
            resultCode: Int,
            data: Intent?,
        ) {
            if (resultCode == Activity.RESULT_OK) {
                setupGoogleAccountCredential(authClient.getAuthorizationResultFromIntent(data))
            } else {
                _authorizationState.value = AuthorizationState.Error(stringRes(R.string.error_auth_denied))
            }
        }

        /**
         * Sets up the Google Account Credential after successful authorization.
         */
        private fun setupGoogleAccountCredential(authResult: AuthorizationResult) {
            val token = authResult.accessToken

            if (token != null) {
                googleAccountToken = AccessToken
                    .newBuilder()
                    .setTokenValue(token)
                    .build()

                _authorizationState.value = AuthorizationState.Authorized
            } else {
                _authorizationState.value = AuthorizationState.Error(stringRes(R.string.error_account_info_failed))
            }
        }

        /**
         * Signs out the current user by clearing the stored token and credentials.
         */
        suspend fun signOut() =
            withContext(dispatcher) {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                googleAccountToken = null
                _authorizationState.value = AuthorizationState.NotAuthorized
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

/**
 * Represents the state of the Google Drive authorization process.
 */
sealed class AuthorizationState {
    data object NotAuthorized : AuthorizationState()

    data object Authorizing : AuthorizationState()

    data object Authorized : AuthorizationState()

    data class Error(
        val message: StringResource,
    ) : AuthorizationState()
}
