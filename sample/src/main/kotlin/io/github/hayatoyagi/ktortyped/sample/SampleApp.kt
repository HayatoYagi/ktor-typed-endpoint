package io.github.hayatoyagi.ktortyped.sample

import io.github.hayatoyagi.ktortyped.sample.routing.authorRoutes
import io.github.hayatoyagi.ktortyped.sample.routing.bookRoutes
import io.ktor.http.ContentType
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.plugin
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.SwaggerConfig
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingRoot
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi

fun main() {
    embeddedServer(Netty, port = 8080) {
        configureSample()
    }.start(wait = true)
}

@OptIn(ExperimentalKtorApi::class)
fun Application.configureSample() {
    install(Resources)
    install(ContentNegotiation) { json() }

    routing {
        swaggerUI(path = "swagger") {
            configureSampleSwagger(this@configureSample)
        }

        get("/openapi.json") {
            val source = call.application.sampleOpenApiSource()
            val doc = source.read(call.application, io.ktor.openapi.OpenApiDoc(info = sampleOpenApiInfo()))
            call.respondText(doc.content, doc.contentType)
        }.hide()

        bookRoutes()
        authorRoutes()
    }
}

fun Application.sampleOpenApiInfo(): OpenApiInfo = OpenApiInfo(
    title = "ktor-typed-endpoint Sample API",
    version = "0.1.0",
    description = "Demonstrates typed endpoint contracts with automatic OpenAPI generation.",
)

fun Application.sampleOpenApiSource(): OpenApiDocSource.Routing = OpenApiDocSource.Routing(
    contentType = ContentType.Application.Json,
) {
    plugin(RoutingRoot).allRoutes()
}

fun SwaggerConfig.configureSampleSwagger(application: Application) {
    info = application.sampleOpenApiInfo()
    source = application.sampleOpenApiSource()
}

private fun Route.allRoutes(): Sequence<Route> = sequence {
    yield(this@allRoutes)
    for (child in children) {
        yieldAll(child.allRoutes())
    }
}
