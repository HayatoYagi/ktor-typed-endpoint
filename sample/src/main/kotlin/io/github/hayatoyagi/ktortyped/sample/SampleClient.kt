package io.github.hayatoyagi.ktortyped.sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Demonstrates using KSP-generated client extension functions.
 *
 * The KSP processor reads the contract objects in this module and generates:
 *
 *   suspend fun HttpClient.getBooks(): BookListResponse
 *   suspend fun HttpClient.getBookById(id: String): BookResponse
 *   suspend fun HttpClient.postBook(body: CreateBookRequest): BookResponse
 *   ... and so on for every contract
 *
 * Callers only import the contract — no manual Resource construction needed.
 */
suspend fun runSampleClient() {
    val client = HttpClient(CIO) {
        install(Resources)
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest {
            url("http://localhost:8080")
            contentType(ContentType.Application.Json)
        }
    }

    client.use {
        // GET /v1/books
        val books = it.getBooks()
        println("Books: $books")

        // POST /v1/books
        val created = it.postBook(body = CreateBookRequest(title = "Kotlin in Action", authorId = "1"))
        println("Created: $created")

        // GET /v1/books/{id}
        val book = it.getBookById(id = created.id)
        println("Fetched: $book")
    }
}
