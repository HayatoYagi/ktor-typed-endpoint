# ktor-typed-endpoint

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hayatoyagi/ktor-typed-endpoint-core)](https://central.sonatype.com/artifact/io.github.hayatoyagi/ktor-typed-endpoint-core)

Type-safe HTTP endpoint contracts for Ktor — bind routing, request/response types, and OpenAPI generation in one place.

## Table of Contents

- [Overview](#overview)
- [Motivation](#motivation)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Features](#features)
  - [@ApiTag — OpenAPI tag inheritance](#apitag--openapi-tag-inheritance)
  - [@ApiDescription — model-driven OpenAPI descriptions](#apidescription--model-driven-openapi-descriptions)
- [Client](#client)
  - [Error handling](#error-handling)
- [Sample App](#sample-app)
- [License](#license)

## Overview

`ktor-typed-endpoint` lets you define each API endpoint as a single typed object that captures:

- The HTTP method (GET / POST / PUT / PATCH / DELETE)
- The Ktor `@Resource` for type-safe path/query parameters
- The request and response body types — deserialized and passed as a typed argument to the handler
- The success HTTP status code

Route registration and OpenAPI documentation are both driven from this one contract, eliminating duplication and keeping the API definition as the single source of truth.

## Motivation

Ktor's [`@Resource`](https://ktor.io/docs/server-resources.html) gives you type-safe path and query parameters. But it stops there — the HTTP method, request/response body types, and success status code have no type-level home.

**Before** — routing, serialization, and documentation are scattered across three separate blocks that must be kept in sync manually:

```kotlin
// 1. Route registration
post<Books> {
    val request = call.receive<CreateBookRequest>()
    val book = createBook(request)
    call.respond(HttpStatusCode.Created, book)
}

// 2. OpenAPI documentation — written separately, easy to forget or get out of sync
describe {
    tag("books")
    requestBody { schema = jsonSchema<CreateBookRequest>() }
    responses {
        HttpStatusCode.Created { schema = jsonSchema<BookResponse>() }
    }
}
```

**After** — one contract object is the single source of truth:

```kotlin
// 1. Define the contract once
object PostBook : PostEndpointContract<Books, CreateBookRequest, BookResponse, ErrorResponse>(
    successStatusCode = HttpStatusCode.Created,
)

// 2. Register the route — routing, serialization, status code, and OpenAPI are all handled
endpoint(PostBook) { _, request ->
    createBook(request)
}
```

## Requirements

- Kotlin 2.4.10+ — earlier compilers may fail to read the published Kotlin/Native, JS, and Wasm klibs for non-JVM/Android targets.
- Ktor 3.5.x — `ktor-typed-endpoint-ktor-server`'s public API is built against Ktor 3.5.2; other Ktor 3.x versions should generally work, but matching your app's Ktor version to the same 3.5.x line is recommended.
- Android: `compileSdk` 36, `minSdk` 21+.

## Installation

```kotlin
// settings.gradle.kts
repositories {
    mavenCentral()
}
```

```kotlin
// shared module (contracts live here — KMP: JVM + Android + iOS + JS/Wasm)
implementation("io.github.hayatoyagi:ktor-typed-endpoint-core:<version>")

// server module (route registration + OpenAPI)
implementation("io.github.hayatoyagi:ktor-typed-endpoint-ktor-server:<version>")

// client module (type-safe HTTP requests with typed error responses)
implementation("io.github.hayatoyagi:ktor-typed-endpoint-ktor-client:<version>")
```

## Quick Start

### 1. Define your resources

```kotlin
@Serializable
@Resource("/v1")
class ApiRoutes {
    @ApiTag("books")
    @Serializable
    @Resource("books")
    class Books(val parent: ApiRoutes = ApiRoutes()) {
        @Serializable
        @Resource("{id}")
        data class ById(val parent: Books = Books(), val id: String)
    }
}
```

### 2. Define contracts

The last type parameter is the error response body — use a shared `ErrorResponse` type,
or `Unit` for endpoints that don't need a typed error body (see [Error handling](#error-handling)).

```kotlin
object GetBooks : GetEndpointContract<ApiRoutes.Books, BookListResponse, ErrorResponse>()

object PostBook : PostEndpointContract<ApiRoutes.Books, CreateBookRequest, BookResponse, ErrorResponse>(
    successStatusCode = HttpStatusCode.Created,
)

object PutBook : PutEndpointContract<ApiRoutes.Books.ById, UpdateBookRequest, BookResponse, ErrorResponse>()

object PatchBook : PatchEndpointContract<ApiRoutes.Books.ById, PatchBookRequest, BookResponse, ErrorResponse>()
```

### 3. Register routes

```kotlin
fun Route.bookRoutes() {
    endpoint(GetBooks) {
        bookRepository.findAll()
    }

    endpoint(PostBook) { _, request ->
        bookRepository.create(request)
    }

    endpoint(PutBook) { resource, request ->
        bookRepository.replace(resource.id, request)
    }

    endpoint(PatchBook) { resource, request ->
        bookRepository.update(resource.id, request)
    }
}
```

Route registration, request deserialization, response serialization, and OpenAPI documentation are all handled automatically.

## Features

### `@ApiTag` — OpenAPI tag inheritance

Annotate a parent resource to group all its endpoints in Swagger UI. Tags are **inherited by child resources** automatically.

```kotlin
@ApiTag("books")           // All /books/** endpoints get the "books" tag
@Serializable
@Resource("books")
class Books(val parent: ApiRoutes = ApiRoutes()) {

    @Serializable
    @Resource("{id}")      // Also tagged "books" — no annotation needed
    data class ById(val parent: Books = Books(), val id: String) {

        @Serializable
        @Resource("reviews") // Also tagged "books"
        class Reviews(val parent: ById)
    }
}
```

### `@ApiDescription` — model-driven OpenAPI descriptions

Annotate request and response models to add descriptions that flow into the generated OpenAPI spec.

```kotlin
@ApiDescription(
    """
    Creates a new book.

    Validation rules:
    - `title` must not be blank.
    - `authorId` must reference an existing author.
    """,
)
@Serializable
data class CreateBookRequest(
    val title: String,
    val authorId: String,
)
```

The description appears as `requestBody.description` for request models and `responses.<status>.description` for response models.

## Client

`ktor-typed-endpoint-ktor-client` provides `HttpClient.request(contract, resource)` extension
functions. The response type is inferred from the contract — no explicit type annotation needed.

```kotlin
val client = HttpClient(CIO) {
    install(Resources)
    install(ContentNegotiation) { json() }
    defaultRequest { url("https://api.example.com") }
}

// Response type (BookResponse) is inferred from GetBookById's contract
val book = client.request(GetBookById, ApiRoutes.Books.ById(id = "42"))

// POST with a request body
val created = client.request(PostBook, ApiRoutes.Books(), CreateBookRequest(title = "Kotlin in Action", authorId = "1"))
```

Compare with plain Ktor, where you specify the response type manually and must know the HTTP method:

```kotlin
// plain Ktor
val book: BookResponse = client.get(ApiRoutes.Books.ById(id = "42")).body()
```

### Error handling

The contract's `Error` type parameter is the single source of truth for both the server's and the
client's understanding of a failure response — not just the success path. When a response's status
doesn't match the contract's `successStatusCode`, `request(...)` throws `EndpointErrorException`
instead of a generic exception:

```kotlin
try {
    client.request(GetBookById, ApiRoutes.Books.ById(id = "missing"))
} catch (e: EndpointErrorException) {
    val error: ErrorResponse? = e.errorBodyAs<ErrorResponse>()
    println("Request failed with ${e.status}: ${error?.message}")
}
```

`errorBodyAs<T>()` does a reified runtime check, so it's safe even though `EndpointErrorException`
itself can't be generic (Kotlin, like Java, forbids a `Throwable` subclass with type parameters).
`errorBody` is best-effort: if the response couldn't be deserialized as the contract's `Error` type,
`errorBodyAs<T>()` returns `null` rather than masking the original failure with a secondary exception.

For endpoints where a typed error body doesn't matter, declare the contract with `Unit`:

```kotlin
object PostAuthor : PostEndpointContract<ApiRoutes.Authors, CreateAuthorRequest, AuthorResponse, Unit>(
    successStatusCode = HttpStatusCode.Created,
)
```

## Sample App

The `sample` module contains a minimal runnable Ktor server that demonstrates all library features — nested resources, tag inheritance, multiple HTTP methods, and `@ApiDescription`.

```bash
./gradlew :sample:run
```

Then open `http://localhost:8080/swagger` to explore the generated API documentation.

## License

Apache-2.0
