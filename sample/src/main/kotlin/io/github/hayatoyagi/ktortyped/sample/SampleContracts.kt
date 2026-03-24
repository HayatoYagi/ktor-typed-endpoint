package io.github.hayatoyagi.ktortyped.sample

import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.PatchEndpointContract
import io.github.hayatoyagi.ktortyped.PostEndpointContract
import io.github.hayatoyagi.ktortyped.PutEndpointContract
import io.ktor.http.HttpStatusCode

// --- Book contracts ---

object GetBooks : GetEndpointContract<SampleRoutes.Books, BookListResponse>()

object GetBookById : GetEndpointContract<SampleRoutes.Books.ById, BookResponse>()

object PostBook : PostEndpointContract<SampleRoutes.Books, CreateBookRequest, BookResponse>(
    successStatusCode = HttpStatusCode.Created,
)

object PutBook : PutEndpointContract<SampleRoutes.Books.ById, UpdateBookRequest, BookResponse>()

object PatchBook : PatchEndpointContract<SampleRoutes.Books.ById, PatchBookRequest, BookResponse>()

// --- Review contracts (tag inherited from Books via parent chain) ---

object GetBookReviews : GetEndpointContract<SampleRoutes.Books.ById.Reviews, ReviewListResponse>()

// --- Author contracts ---

object GetAuthorById : GetEndpointContract<SampleRoutes.Authors.ById, AuthorResponse>()

object PostAuthor : PostEndpointContract<SampleRoutes.Authors, CreateAuthorRequest, AuthorResponse>(
    successStatusCode = HttpStatusCode.Created,
)
