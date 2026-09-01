package io.github.hayatoyagi.ktortyped.sample.server.routing

import io.github.hayatoyagi.ktortyped.sample.contracts.AuthorResponse
import io.github.hayatoyagi.ktortyped.sample.contracts.GetAuthorById
import io.github.hayatoyagi.ktortyped.sample.contracts.PostAuthor
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
