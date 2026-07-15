package com.mojtaba.pocketledger.account

import android.content.Context
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import com.mojtaba.pocketledger.core.security.passkey.PasskeyAuthenticationOptions
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClient
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClientAvailability
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClientError
import com.mojtaba.pocketledger.core.security.passkey.PasskeyClientResult
import com.mojtaba.pocketledger.core.security.passkey.PasskeyCredentialResponse
import com.mojtaba.pocketledger.core.security.passkey.PasskeyRegistrationOptions
import com.mojtaba.pocketledger.core.security.passkey.PasskeyUnavailableReason

class AndroidCredentialManagerPasskeyClient(
    context: Context,
) : PasskeyClient {
    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    override fun availability(): PasskeyClientAvailability = PasskeyClientAvailability.Available

    override suspend fun register(options: PasskeyRegistrationOptions): PasskeyClientResult =
        runCatching {
            val request = CreatePublicKeyCredentialRequest(
                requestJson = options.requestJson,
                preferImmediatelyAvailableCredentials = false,
            )
            credentialManager.createCredential(
                context = appContext,
                request = request,
            )
        }.fold(
            onSuccess = { response ->
                PasskeyClientResult.Success(
                    PasskeyCredentialResponse(responseJson = response.data.toString()),
                )
            },
            onFailure = { throwable -> throwable.toPasskeyResult() },
        )

    override suspend fun authenticate(options: PasskeyAuthenticationOptions): PasskeyClientResult =
        runCatching {
            val request = GetCredentialRequest(
                credentialOptions = listOf(
                    GetPublicKeyCredentialOption(requestJson = options.requestJson),
                ),
            )
            credentialManager.getCredential(
                context = appContext,
                request = request,
            )
        }.fold(
            onSuccess = { response ->
                val credential = response.credential
                if (credential is PublicKeyCredential) {
                    PasskeyClientResult.Success(
                        PasskeyCredentialResponse(responseJson = credential.authenticationResponseJson),
                    )
                } else {
                    PasskeyClientResult.Failure(
                        error = PasskeyClientError.ProviderFailure,
                        safeMessage = "Credential provider returned a non-passkey credential.",
                    )
                }
            },
            onFailure = { throwable -> throwable.toPasskeyResult() },
        )

    private fun Throwable.toPasskeyResult(): PasskeyClientResult = when (this) {
        is CreateCredentialCancellationException,
        is GetCredentialCancellationException,
        -> PasskeyClientResult.Failure(
            error = PasskeyClientError.Cancelled,
            safeMessage = "Passkey operation was cancelled.",
        )
        is CreateCredentialException,
        is GetCredentialException,
        -> PasskeyClientResult.Failure(
            error = PasskeyClientError.ProviderFailure,
            safeMessage = "Credential Manager could not complete the passkey operation.",
        )
        is IllegalArgumentException -> PasskeyClientResult.Failure(
            error = PasskeyClientError.InvalidRequest,
            safeMessage = "Passkey request was invalid.",
        )
        else -> PasskeyClientResult.Failure(
            error = PasskeyClientError.Unknown,
            safeMessage = "Passkey operation failed.",
        )
    }
}