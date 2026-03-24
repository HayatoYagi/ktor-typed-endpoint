package io.github.hayatoyagi.ktortyped.sample

import io.github.hayatoyagi.ktortyped.ApiDescription
import kotlinx.serialization.Serializable

// --- Book models ---

@Serializable
data class BookResponse(
    val id: String,
    val title: String,
    val authorId: String,
)

@Serializable
data class BookListResponse(val books: List<BookResponse>)

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

@ApiDescription("Replaces all fields of an existing book.")
@Serializable
data class UpdateBookRequest(
    val title: String,
    val authorId: String,
)

@ApiDescription("All fields are optional. Only provided fields are updated.")
@Serializable
data class PatchBookRequest(
    val title: String? = null,
    val authorId: String? = null,
)

// --- Review models ---

@Serializable
data class ReviewResponse(
    val id: String,
    val bookId: String,
    val body: String,
    val rating: Int,
)

@Serializable
data class ReviewListResponse(val reviews: List<ReviewResponse>)

// --- Author models ---

@Serializable
data class AuthorResponse(
    val id: String,
    val name: String,
)

@ApiDescription(
    """
    Creates a new author.

    Validation rules:
    - `name` must not be blank.
    """,
)
@Serializable
data class CreateAuthorRequest(val name: String)
