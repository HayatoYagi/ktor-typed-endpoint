package io.github.hayatoyagi.ktortyped.sample

import io.github.hayatoyagi.ktortyped.ApiTag
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/v1")
class SampleRoutes {

    @ApiTag("books")
    @Serializable
    @Resource("books")
    class Books(val parent: SampleRoutes = SampleRoutes()) {

        @Serializable
        @Resource("{id}")
        data class ById(val parent: Books = Books(), val id: String) {

            // Inherits the "books" tag from Books via the parent chain
            @Serializable
            @Resource("reviews")
            class Reviews(val parent: ById)
        }
    }

    @ApiTag("authors")
    @Serializable
    @Resource("authors")
    class Authors(val parent: SampleRoutes = SampleRoutes()) {

        @Serializable
        @Resource("{id}")
        data class ById(val parent: Authors = Authors(), val id: String)
    }
}
