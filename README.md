# ktor-typed-endpoint

Type-safe HTTP endpoint contracts for Ktor — bind routing, request/response types, and OpenAPI generation in one place.

## Motivation

Ktor's [`@Resource`](https://ktor.io/docs/server-resources.html) gives you type-safe path and query parameters. But it stops there — the HTTP method, request/response body types, and success status code have no type-level home. Without this library, a typical endpoint looks like this:

```kotlin
// Route registration
post<Books> {
    val request = call.receive<CreateBookRequest>()  // type lives here...
    val book = createBook(request)
    call.respond(HttpStatusCode.Created, book)       // ...status code here...
}

// OpenAPI documentation — written separately, easy to forget or get out of sync
describe {
    tag("books")
    requestBody { schema = jsonSchema<CreateBookRequest>() }  // ...and duplicated here
    responses {
        HttpStatusCode.Created { schema = jsonSchema<BookResponse>() }
    }
}
```

Three concerns — routing, serialization, and documentation — are scattered and must be kept in sync manually. If you change the response type or status code in one place, nothing stops the other from drifting.

`ktor-typed-endpoint` captures all of it in one contract object:

```kotlin
object PostBook : PostEndpointContract<Books, CreateBookRequest, BookResponse>(
    successStatusCode = HttpStatusCode.Created,
)
```

Then a single `endpoint(PostBook) { }` call registers the route, deserializes the request, serializes the response, sets the status code, and generates the OpenAPI documentation — all from the contract.

## Overview

`ktor-typed-endpoint` lets you define each API endpoint as a single typed object that captures:

- The HTTP method (GET / POST / PUT / PATCH / DELETE)
- The Ktor `@Resource` for type-safe path/query parameters
- The request and response body types
- The success HTTP status code

Route registration and OpenAPI documentation are both driven from this one contract, eliminating duplication and keeping the API definition as the single source of truth.

## Installation

```kotlin
// settings.gradle.kts
repositories {
    mavenCentral()
}
```

```kotlin
// shared module (contracts live here — KMP: JVM + Android)
implementation("io.github.hayatoyagi:ktor-typed-endpoint-core:<version>")

// server module (route registration + OpenAPI)
implementation("io.github.hayatoyagi:ktor-typed-endpoint-ktor-server:<version>")
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

```kotlin
object GetBooks : GetEndpointContract<ApiRoutes.Books, BookListResponse>()

object PostBook : PostEndpointContract<ApiRoutes.Books, CreateBookRequest, BookResponse>(
    successStatusCode = HttpStatusCode.Created,
)

object PutBook : PutEndpointContract<ApiRoutes.Books.ById, UpdateBookRequest, BookResponse>()

object PatchBook : PatchEndpointContract<ApiRoutes.Books.ById, PatchBookRequest, BookResponse>()
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

## Sample App

The `sample` module contains a minimal runnable Ktor server that demonstrates all library features — nested resources, tag inheritance, multiple HTTP methods, and `@ApiDescription`.

```bash
./gradlew :sample:run
```

Then open `http://localhost:8080/swagger` to explore the generated API documentation.

## License

Apache-2.0
