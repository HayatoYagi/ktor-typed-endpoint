package io.github.hayatoyagi.ktortyped.sample.routing

import io.github.hayatoyagi.ktortyped.sample.AuthorResponse
import io.github.hayatoyagi.ktortyped.sample.GetAuthorById
import io.github.hayatoyagi.ktortyped.sample.PostAuthor
import io.github.hayatoyagi.ktortyped.server.endpoint
import io.ktor.server.routing.Route

fun Route.authorRoutes() {
    endpoint(GetAuthorById) { resource ->
        AuthorResponse(resource.id, "Author ${resource.id}")
    }

    endpoint(PostAuthor) { _, request ->
        AuthorResponse(
            id = "new-${System.currentTimeMillis()}",
            name = request.name,
        )
    }
}
