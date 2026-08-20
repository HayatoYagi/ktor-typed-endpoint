package io.github.hayatoyagi.ktortyped.client

import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.server.endpoint
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.plugins.resources.Resources as ClientResources
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// --- Test fixtures ---

@Serializable
@Resource("/items")
class Items {
    @Serializable
    @Resource("{id}")
    data class ById(val parent: Items = Items(), val id: String)

    @Serializable
    @Resource("no-typed-error/{id}")
    data class ByIdNoTypedError(val parent: Items = Items(), val id: String)
}

@Serializable
data class ItemResponse(val id: String, val name: String)

@Serializable
data class ItemErrorResponse(val message: String)

class ItemNotFoundException(val id: String) : Exception("Item not found: $id")

object GetItemById : GetEndpointContract<Items.ById, ItemResponse, ItemErrorResponse>()
object GetItemByIdNoTypedError : GetEndpointContract<Items.ByIdNoTypedError, ItemResponse, Unit>()

private fun withTestApp(block: suspend HttpClient.() -> Unit) = testApplication {
    application {
        install(Resources)
        install(ContentNegotiation) { json() }
        install(StatusPages) {
            exception<ItemNotFoundException> { call, cause ->
                call.respond(HttpStatusCode.NotFound, ItemErrorResponse(cause.message ?: "not found"))
            }
        }
        routing {
            endpoint(GetItemById) { resource ->
                if (resource.id == "missing") throw ItemNotFoundException(resource.id)
                ItemResponse(resource.id, "item-${resource.id}")
            }
            endpoint(GetItemByIdNoTypedError) { resource ->
                if (resource.id == "missing") throw ItemNotFoundException(resource.id)
                ItemResponse(resource.id, "item-${resource.id}")
            }
        }
    }
    val client = createClient {
        install(ClientResources)
        install(ClientContentNegotiation) { json() }
    }
    client.block()
}

// --- Client tests ---

class EndpointClientTest {
    @Test
    fun `successful GET returns the typed response`() = withTestApp {
        val response = request(GetItemById, Items.ById(id = "abc"))
        assertEquals("abc", response.id)
    }

    @Test
    fun `failed GET throws EndpointErrorException with typed error body`() = withTestApp {
        val exception = assertFailsWith<EndpointErrorException> {
            request(GetItemById, Items.ById(id = "missing"))
        }
        assertEquals(HttpStatusCode.NotFound, exception.status)
        assertEquals("Item not found: missing", exception.errorBodyAs<ItemErrorResponse>()?.message)
    }

    @Test
    fun `Unit error type does not crash on failure`() = withTestApp {
        val exception = assertFailsWith<EndpointErrorException> {
            request(GetItemByIdNoTypedError, Items.ByIdNoTypedError(id = "missing"))
        }
        assertEquals(HttpStatusCode.NotFound, exception.status)
        assertEquals(Unit, exception.errorBody)
    }
}
