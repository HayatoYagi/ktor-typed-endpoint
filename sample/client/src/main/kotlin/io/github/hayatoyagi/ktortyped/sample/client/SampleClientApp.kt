package io.github.hayatoyagi.ktortyped.sample.client

import io.github.hayatoyagi.ktortyped.client.EndpointErrorException
import io.github.hayatoyagi.ktortyped.client.errorBodyAs
import io.github.hayatoyagi.ktortyped.client.request
import io.github.hayatoyagi.ktortyped.sample.contracts.ErrorResponse
import io.github.hayatoyagi.ktortyped.sample.contracts.GetBookById
import io.github.hayatoyagi.ktortyped.sample.contracts.GetBooks
import io.github.hayatoyagi.ktortyped.sample.contracts.SampleRoutes
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking

/**
 * Talks to the running `:sample:server` over real HTTP, using the exact same contract objects
 * (`:sample:contracts`) the server registers its routes from — the point of this split is to
 * demonstrate the contract as a shared source of truth, not just unit-test the client in isolation.
 *
 * Run `:sample:server:run` first, then this.
 */
fun main() = runBlocking {
    val client = HttpClient(CIO) {
        install(Resources)
        install(ContentNegotiation) { json() }
        defaultRequest { url("http://localhost:8080/") }
    }

    println("GET /v1/books")
    println(client.request(GetBooks, SampleRoutes.Books()))

    println("GET /v1/books/1")
    println(client.request(GetBookById, SampleRoutes.Books.ById(id = "1")))

    println("GET /v1/books/missing (triggers the typed error path)")
    try {
        client.request(GetBookById, SampleRoutes.Books.ById(id = "missing"))
    } catch (e: EndpointErrorException) {
        println("EndpointErrorException: status=${e.status} error=${e.errorBodyAs<ErrorResponse>()}")
    }

    client.close()
}
