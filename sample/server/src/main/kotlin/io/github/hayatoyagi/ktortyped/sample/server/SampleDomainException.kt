package io.github.hayatoyagi.ktortyped.sample.server

import io.github.hayatoyagi.ktortyped.sample.contracts.ErrorResponse
import io.ktor.http.HttpStatusCode

/**
 * Base type for this sample app's domain exceptions, each carrying a structured [ErrorResponse].
 *
 * This is a sample-app convention, not a library concept — `core`/`ktor-server`/`ktor-client`
 * know nothing about it. A single `StatusPages` handler for this type (see
 * `Application.configureSample`) covers every subclass, so adding a new domain error never
 * requires touching `SampleApp.kt`.
 */
abstract class SampleDomainException(val status: HttpStatusCode, val error: ErrorResponse) : Exception(error.message)
