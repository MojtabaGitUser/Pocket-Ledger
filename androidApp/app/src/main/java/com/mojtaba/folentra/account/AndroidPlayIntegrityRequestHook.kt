package com.mojtaba.folentra.account

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.mojtaba.folentra.core.security.integrity.PlayIntegrityAvailability
import com.mojtaba.folentra.core.security.integrity.PlayIntegrityError
import com.mojtaba.folentra.core.security.integrity.PlayIntegrityRequest
import com.mojtaba.folentra.core.security.integrity.PlayIntegrityRequestHook
import com.mojtaba.folentra.core.security.integrity.PlayIntegrityTokenResult
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidPlayIntegrityRequestHook(
    context: Context,
) : PlayIntegrityRequestHook {
    private val integrityManager = IntegrityManagerFactory.create(context.applicationContext)

    override fun availability(): PlayIntegrityAvailability = PlayIntegrityAvailability.Available

    override suspend fun requestToken(request: PlayIntegrityRequest): PlayIntegrityTokenResult =
        runCatching {
            val tokenRequestBuilder = IntegrityTokenRequest.builder()
                .setNonce(request.nonce)
            request.cloudProjectNumber?.let(tokenRequestBuilder::setCloudProjectNumber)
            integrityManager.requestIntegrityToken(tokenRequestBuilder.build()).await()
        }.fold(
            onSuccess = { response -> PlayIntegrityTokenResult.Success(token = response.token()) },
            onFailure = { throwable ->
                val error = if (throwable is IllegalArgumentException) {
                    PlayIntegrityError.InvalidRequest
                } else {
                    PlayIntegrityError.ProviderFailure
                }
                PlayIntegrityTokenResult.Failure(
                    error = error,
                    safeMessage = "Play Integrity token request failed.",
                )
            },
        )

    private suspend fun Task<IntegrityTokenResponse>.await(): IntegrityTokenResponse =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { response ->
                if (continuation.isActive) continuation.resume(response)
            }
            addOnFailureListener { throwable ->
                if (continuation.isActive) continuation.resumeWith(Result.failure(throwable))
            }
            addOnCanceledListener {
                if (continuation.isActive) {
                    continuation.resumeWith(Result.failure(IllegalStateException("Play Integrity request was cancelled.")))
                }
            }
        }
}