package io.github.hayatoyagi.ktortyped.server

import io.github.hayatoyagi.ktortyped.ApiDescription
import io.github.hayatoyagi.ktortyped.ApiTag
import io.github.hayatoyagi.ktortyped.DeleteEndpointContract
import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.PatchEndpointContract
import io.github.hayatoyagi.ktortyped.PostEndpointContract
import io.github.hayatoyagi.ktortyped.PutEndpointContract
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiInfo
import io.ktor.resources.Resource
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.plugin
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingRoot
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// --- Test fixtures ---

@Serializable
@Resource("/items")
class Items {
    @Serializable
    @Resource("{id}")
    data class ById(val parent: Items = Items(), val id: String)
}

@ApiTag("my-tag")
@Serializable
@Resource("/tagged")
class Tagged {
    @Serializable
    @Resource("{id}")
    data class ById(val parent: Tagged = Tagged(), val id: String)
}

@Serializable
data class ItemResponse(val id: String, val name: String)

@Serializable
data class ItemListResponse(val items: List<ItemResponse>)

@ApiDescription("Creates a new item. The name must not be blank.")
@Serializable
data class CreateItemRequest(val name: String)

@ApiDescription("The created or updated item.")
@Serializable
data class ItemCreatedResponse(val id: String, val name: String)

@Serializable
data class UpdateItemRequest(val name: String)

@Serializable
data class PatchItemRequest(val name: String?)

@Serializable
data class DeletedResponse(val deleted: Boolean)

object GetItems : GetEndpointContract<Items, ItemListResponse>()
object GetItemById : GetEndpointContract<Items.ById, ItemResponse>()
object PostItem : PostEndpointContract<Items, CreateItemRequest, ItemCreatedResponse>()
object PutItem : PutEndpointContract<Items.ById, UpdateItemRequest, ItemResponse>()
object PatchItem : PatchEndpointContract<Items.ById, PatchItemRequest, ItemResponse>()
object DeleteItem : DeleteEndpointContract<Items.ById, DeletedResponse>()
object GetTagged : GetEndpointContract<Tagged, ItemListResponse>()
object GetTaggedById : GetEndpointContract<Tagged.ById, ItemResponse>()

@OptIn(ExperimentalKtorApi::class)
private fun Application.openApiSource() = OpenApiDocSource.Routing(
    contentType = ContentType.Application.Json,
) {
    plugin(RoutingRoot).allRoutes()
}

private fun Route.allRoutes(): Sequence<Route> = sequence {
    yield(this@allRoutes)
    for (child in children) yieldAll(child.allRoutes())
}

private fun withTestApp(block: suspend io.ktor.server.testing.ApplicationTestBuilder.() -> Unit) =
    testApplication {
        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                endpoint(GetItems) { ItemListResponse(emptyList()) }
                endpoint(GetItemById) { res -> ItemResponse(res.id, "test") }
                endpoint(PostItem) { _, req -> ItemCreatedResponse("new-id", req.name) }
                endpoint(PutItem) { res, req -> ItemResponse(res.id, req.name) }
                endpoint(PatchItem) { res, req -> ItemResponse(res.id, req.name ?: "unchanged") }
                endpoint(DeleteItem) { DeletedResponse(true) }
                endpoint(GetTagged) { ItemListResponse(emptyList()) }
                endpoint(GetTaggedById) { res -> ItemResponse(res.id, "tagged") }
                get("/openapi.json") {
                    val source = call.application.openApiSource()
                    val doc = source.read(call.application, OpenApiDoc(info = OpenApiInfo(title = "test", version = "0")))
                    call.respondText(doc.content, doc.contentType)
                }.hide()
            }
        }
        block()
    }

// --- Routing tests ---

class EndpointRoutingTest {
    @Test
    fun `GET endpoint returns 200 with response body`() = withTestApp {
        val response = client.get("/items/abc")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<ItemResponse>(response.bodyAsText())
        assertEquals("abc", body.id)
    }

    @Test
    fun `GET list endpoint returns 200`() = withTestApp {
        val response = client.get("/items")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST endpoint returns 201 with response body`() = withTestApp {
        val response = client.post("/items") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"my book"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.decodeFromString<ItemCreatedResponse>(response.bodyAsText())
        assertEquals("my book", body.name)
    }

    @Test
    fun `PUT endpoint returns 200 with response body`() = withTestApp {
        val response = client.put("/items/abc") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"updated"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<ItemResponse>(response.bodyAsText())
        assertEquals("updated", body.name)
    }

    @Test
    fun `PATCH endpoint returns 200 with response body`() = withTestApp {
        val response = client.patch("/items/abc") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"patched"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<ItemResponse>(response.bodyAsText())
        assertEquals("patched", body.name)
    }

    @Test
    fun `DELETE endpoint returns 200 with response body`() = withTestApp {
        val response = client.delete("/items/abc")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<DeletedResponse>(response.bodyAsText())
        assertTrue(body.deleted)
    }
}

// --- OpenAPI description tests ---

class EndpointDescriptionsTest {
    @Test
    fun `openapi spec contains all registered routes`() = withTestApp {
        val json = client.get("/openapi.json").bodyAsText()
            .let { Json.parseToJsonElement(it).jsonObject }
        val paths = json["paths"]!!.jsonObject
        assertTrue(paths.containsKey("/items"))
        assertTrue(paths.containsKey("/items/{id}"))
        assertTrue(paths.containsKey("/tagged"))
        assertTrue(paths.containsKey("/tagged/{id}"))
    }

    @Test
    fun `GET endpoint generates response schema`() = withTestApp {
        val paths = Json.parseToJsonElement(client.get("/openapi.json").bodyAsText()).jsonObject["paths"]!!.jsonObject
        val responses = paths["/items/{id}"]!!.jsonObject["get"]!!.jsonObject["responses"]!!.jsonObject
        assertTrue(responses.containsKey("200"))
        val schema = responses["200"]!!.jsonObject["content"]?.jsonObject?.get("application/json")?.jsonObject?.get("schema")
        assertNotNull(schema)
    }

    @Test
    fun `POST endpoint generates request body and 201 response`() = withTestApp {
        val paths = Json.parseToJsonElement(client.get("/openapi.json").bodyAsText()).jsonObject["paths"]!!.jsonObject
        val post = paths["/items"]!!.jsonObject["post"]!!.jsonObject
        assertNotNull(post["requestBody"])
        assertTrue(post["responses"]!!.jsonObject.containsKey("201"))
    }

    @Test
    fun `ApiDescription flows into openapi requestBody description`() = withTestApp {
        val paths = Json.parseToJsonElement(client.get("/openapi.json").bodyAsText()).jsonObject["paths"]!!.jsonObject
        val requestBody = paths["/items"]!!.jsonObject["post"]!!.jsonObject["requestBody"]!!.jsonObject
        val description = requestBody["description"]?.jsonPrimitive?.content
        assertNotNull(description)
        assertTrue(description.contains("Creates a new item"))
    }

    @Test
    fun `ApiDescription flows into openapi response description`() = withTestApp {
        val paths = Json.parseToJsonElement(client.get("/openapi.json").bodyAsText()).jsonObject["paths"]!!.jsonObject
        val responses = paths["/items"]!!.jsonObject["post"]!!.jsonObject["responses"]!!.jsonObject
        val description = responses["201"]!!.jsonObject["description"]?.jsonPrimitive?.content
        assertNotNull(description)
        assertTrue(description.contains("created or updated"))
    }

    @Test
    fun `ApiTag is applied to tagged resource`() = withTestApp {
        val paths = Json.parseToJsonElement(client.get("/openapi.json").bodyAsText()).jsonObject["paths"]!!.jsonObject
        val tags = paths["/tagged"]!!.jsonObject["get"]!!.jsonObject["tags"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertTrue(tags.contains("my-tag"))
    }

    @Test
    fun `ApiTag is inherited by child resources`() = withTestApp {
        val paths = Json.parseToJsonElement(client.get("/openapi.json").bodyAsText()).jsonObject["paths"]!!.jsonObject
        val tags = paths["/tagged/{id}"]!!.jsonObject["get"]!!.jsonObject["tags"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertTrue(tags.contains("my-tag"), "child resource should inherit @ApiTag from parent")
    }

    @Test
    fun `tags are not duplicated`() = withTestApp {
        val paths = Json.parseToJsonElement(client.get("/openapi.json").bodyAsText()).jsonObject["paths"]!!.jsonObject
        val tags = paths["/tagged"]!!.jsonObject["get"]!!.jsonObject["tags"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertEquals(tags.distinct(), tags)
    }
}
