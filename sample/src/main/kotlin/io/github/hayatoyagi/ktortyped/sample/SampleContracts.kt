package io.github.hayatoyagi.ktortyped.sample

import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.PatchEndpointContract
import io.github.hayatoyagi.ktortyped.PostEndpointContract
import io.github.hayatoyagi.ktortyped.PutEndpointContract
import io.ktor.http.HttpStatusCode

// --- Book contracts ---

object GetBooks : GetEndpointContract<SampleRoutes.Books, BookListResponse, Unit>()

// Uses a typed error body: GET /books/{id} responds 404 + ErrorResponse for an unknown id.
object GetBookById : GetEndpointContract<SampleRoutes.Books.ById, BookResponse, ErrorResponse>()

object PostBook : PostEndpointContract<SampleRoutes.Books, CreateBookRequest, BookResponse, Unit>(
    successStatusCode = HttpStatusCode.Created,
)

object PutBook : PutEndpointContract<SampleRoutes.Books.ById, UpdateBookRequest, BookResponse, Unit>()

object PatchBook : PatchEndpointContract<SampleRoutes.Books.ById, PatchBookRequest, BookResponse, Unit>()

// --- Review contracts (tag inherited from Books via parent chain) ---

object GetBookReviews : GetEndpointContract<SampleRoutes.Books.ById.Reviews, ReviewListResponse, Unit>()

// --- Author contracts ---

object GetAuthorById : GetEndpointContract<SampleRoutes.Authors.ById, AuthorResponse, Unit>()

// Uses Unit for its error body: this endpoint doesn't need a typed error, just a status code.
object PostAuthor : PostEndpointContract<SampleRoutes.Authors, CreateAuthorRequest, AuthorResponse, Unit>(
    successStatusCode = HttpStatusCode.Created,
)
