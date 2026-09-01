package io.github.hayatoyagi.ktortyped.client

import io.ktor.http.HttpStatusCode

/**
 * Thrown when an endpoint response's status does not match the contract's declared
 * [io.github.hayatoyagi.ktortyped.EndpointContract.successStatusCode].
 *
 * [errorBody] is best-effort: if the response body couldn't be deserialized as the
 * contract's `Error` type (empty body, wrong shape, missing `ContentNegotiation`, etc.)
 * this is `null` rather than masking the original failure with a secondary exception.
 * Use [errorBodyAs] to recover a typed value.
 *
 * This class cannot itself be generic over the `Error` type — Kotlin (like Java)
 * forbids a `Throwable` subclass from declaring type parameters.
 */
class EndpointErrorException(
    val status: HttpStatusCode,
    val errorBody: Any?,
) : Exception("Request failed with status $status")

/** Type-safe access to [EndpointErrorException.errorBody] via a reified runtime check. */
inline fun <reified Error : Any> EndpointErrorException.errorBodyAs(): Error? = errorBody as? Error
