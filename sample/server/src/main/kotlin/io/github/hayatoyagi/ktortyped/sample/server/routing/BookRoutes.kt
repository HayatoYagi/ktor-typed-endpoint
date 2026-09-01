package io.github.hayatoyagi.ktortyped.sample.server.routing

import io.github.hayatoyagi.ktortyped.sample.contracts.BookListResponse
import io.github.hayatoyagi.ktortyped.sample.contracts.BookResponse
import io.github.hayatoyagi.ktortyped.sample.contracts.ErrorResponse
import io.github.hayatoyagi.ktortyped.sample.contracts.GetBookById
import io.github.hayatoyagi.ktortyped.sample.contracts.GetBookReviews
import io.github.hayatoyagi.ktortyped.sample.contracts.GetBooks
import io.github.hayatoyagi.ktortyped.sample.contracts.PatchBook
import io.github.hayatoyagi.ktortyped.sample.contracts.PostBook
import io.github.hayatoyagi.ktortyped.sample.contracts.PutBook
import io.github.hayatoyagi.ktortyped.sample.contracts.ReviewListResponse
import io.github.hayatoyagi.ktortyped.sample.server.SampleDomainException
import io.github.hayatoyagi.ktortyped.server.endpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route

/** Thrown by [bookRoutes] for an unknown book id; mapped to its [error] response by the generic [SampleDomainException] handler. */
class BookNotFoundException(id: String) : SampleDomainException(
    status = HttpStatusCode.NotFound,
    error = ErrorResponse(code = "BOOK_NOT_FOUND", message = "Book not found: $id"),
)

fun Route.bookRoutes() {
    endpoint(GetBooks) {
        // In a real app, this would call a use case or repository
        BookListResponse(
            books = listOf(
                BookResponse("1", "The Pragmatic Programmer", "author-1"),
                BookResponse("2", "Clean Code", "author-2"),
            ),
        )
    }

    endpoint(GetBookById) { resource ->
        if (resource.id == "missing") throw BookNotFoundException(resource.id)
        BookResponse(resource.id, "Sample Book ${resource.id}", "author-1")
    }

    endpoint(PostBook) { _, request ->
        BookResponse(
            id = "new-${System.currentTimeMillis()}",
            title = request.title,
            authorId = request.authorId,
        )
    }

    endpoint(PutBook) { resource, request ->
        BookResponse(resource.id, request.title, request.authorId)
    }

    endpoint(PatchBook) { resource, request ->
        BookResponse(
            id = resource.id,
            title = request.title ?: "Existing Title",
            authorId = request.authorId ?: "existing-author",
        )
    }

    endpoint(GetBookReviews) { resource ->
        ReviewListResponse(emptyList())
    }
}
