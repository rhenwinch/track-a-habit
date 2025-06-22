package io.track.habit.ui.utils

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val BIOMETRICS_TAG = "TaH-Biometrics"
private const val AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

fun Context.authenticate(
    prompt: BiometricPrompt.PromptInfo,
    onAuthSucceed: (BiometricPrompt.AuthenticationResult) -> Unit,
    onAuthFailed: () -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(this)
    val biometricPrompt = BiometricPrompt(
        this as FragmentActivity, // Might throw ClassCastException if context is not a FragmentActivity
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSucceed(result)
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence,
            ) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(BIOMETRICS_TAG, "Error [$errorCode]: $errorCode")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthFailed()
            }
        },
    )

    biometricPrompt.authenticate(prompt)
}

fun Context.isBiometricAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val kgm = ContextCompat.getSystemService(this, KeyguardManager::class.java)

        if (kgm?.isDeviceSecure == false) {
            Log.w(BIOMETRICS_TAG, "Device is not secure")
            return false
        }

        kgm?.isDeviceSecure == true
    } else {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> {
                Log.w(BIOMETRICS_TAG, "Biometric authentication is not available")
                false
            }
        }
    }
}

fun getBiometricsPromptInfo(
    title: String,
    subtitle: String,
    negativeButtonText: String,
): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo
        .Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText)
        .build()
}
