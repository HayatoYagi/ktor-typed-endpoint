package io.github.hayatoyagi.ktortyped.sample

import io.ktor.http.HttpStatusCode

/**
 * Base type for domain exceptions that carry a structured [ErrorResponse].
 *
 * A single `StatusPages` handler for this type (see `Application.configureSample`) covers
 * every subclass, so adding a new domain error never requires touching `SampleApp.kt`.
 */
abstract class ApiException(val status: HttpStatusCode, val error: ErrorResponse) : Exception(error.message)
