package com.wallstreet.presentation.auth.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.wallstreet.R
import timber.log.Timber

/**
 * Single source of truth for the legacy GMS Google Sign-In flow, shared by Login and
 * Register. Returns a trigger lambda; invoking it launches the account picker.
 *
 * - [onIdToken]  successful sign-in with a usable ID token
 * - [onCancel]   user backed out (no error should be shown)
 * - [onError]    anything else (API failure, missing token)
 */
@Composable
fun rememberGoogleSignInLauncher(
    onIdToken: (String) -> Unit,
    onError: () -> Unit,
    onCancel: () -> Unit = {},
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            onCancel()
            return@rememberLauncherForActivityResult
        }
        try {
            val account = GoogleSignIn
                .getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val token = account.idToken
            if (token != null) onIdToken(token) else onError()
        } catch (e: ApiException) {
            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                onCancel()
            } else {
                Timber.e(e, "Google Sign-In failed")
                onError()
            }
        }
    }

    return {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        launcher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
    }
}
