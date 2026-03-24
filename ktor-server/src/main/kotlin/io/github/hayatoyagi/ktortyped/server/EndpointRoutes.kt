package io.github.hayatoyagi.ktortyped.server

import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.head
import io.ktor.server.resources.options
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.github.hayatoyagi.ktortyped.DeleteEndpointContract
import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.HeadEndpointContract
import io.github.hayatoyagi.ktortyped.OptionsEndpointContract
import io.github.hayatoyagi.ktortyped.PatchEndpointContract
import io.github.hayatoyagi.ktortyped.PostEndpointContract
import io.github.hayatoyagi.ktortyped.PutEndpointContract

/**
 * Registers a `GET` route from a [GetEndpointContract].
 *
 * Automatically responds with [GetEndpointContract.successStatusCode] and
 * attaches OpenAPI documentation generated from the contract.
 */
inline fun <reified Resource : Any, reified Response : Any> Route.endpoint(
    contract: GetEndpointContract<Resource, Response>,
    noinline body: suspend RoutingContext.(Resource) -> Response,
): Route = get<Resource> { resource ->
    val response = body(resource)
    call.respond(contract.successStatusCode, response)
}.describeContract(contract)

/**
 * Registers a `POST` route from a [PostEndpointContract].
 *
 * Automatically deserializes the request body, responds with [PostEndpointContract.successStatusCode],
 * and attaches OpenAPI documentation generated from the contract.
 */
inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> Route.endpoint(
    contract: PostEndpointContract<Resource, Request, Response>,
    noinline body: suspend RoutingContext.(resource: Resource, request: Request) -> Response,
): Route = post<Resource> { resource ->
    val request = call.receive<Request>()
    val response = body(resource, request)
    call.respond(contract.successStatusCode, response)
}.describeContract(contract)

/**
 * Registers a `PUT` route from a [PutEndpointContract].
 *
 * Automatically deserializes the request body, responds with [PutEndpointContract.successStatusCode],
 * and attaches OpenAPI documentation generated from the contract.
 */
inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> Route.endpoint(
    contract: PutEndpointContract<Resource, Request, Response>,
    noinline body: suspend RoutingContext.(resource: Resource, request: Request) -> Response,
): Route = put<Resource> { resource ->
    val request = call.receive<Request>()
    val response = body(resource, request)
    call.respond(contract.successStatusCode, response)
}.describeContract(contract)

/**
 * Registers a `PATCH` route from a [PatchEndpointContract].
 *
 * Automatically deserializes the request body, responds with [PatchEndpointContract.successStatusCode],
 * and attaches OpenAPI documentation generated from the contract.
 */
inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> Route.endpoint(
    contract: PatchEndpointContract<Resource, Request, Response>,
    noinline body: suspend RoutingContext.(resource: Resource, request: Request) -> Response,
): Route = patch<Resource> { resource ->
    val request = call.receive<Request>()
    val response = body(resource, request)
    call.respond(contract.successStatusCode, response)
}.describeContract(contract)

/**
 * Registers a `DELETE` route from a [DeleteEndpointContract].
 *
 * Automatically responds with [DeleteEndpointContract.successStatusCode] and
 * attaches OpenAPI documentation generated from the contract.
 */
inline fun <reified Resource : Any, reified Response : Any> Route.endpoint(
    contract: DeleteEndpointContract<Resource, Response>,
    noinline body: suspend RoutingContext.(Resource) -> Response,
): Route = delete<Resource> { resource ->
    val response = body(resource)
    call.respond(contract.successStatusCode, response)
}.describeContract(contract)

/**
 * Registers a `HEAD` route from a [HeadEndpointContract].
 *
 * HEAD responses have no body. The handler should set response headers as needed,
 * then return. Responds with [HeadEndpointContract.successStatusCode] automatically.
 */
inline fun <reified Resource : Any> Route.endpoint(
    contract: HeadEndpointContract<Resource>,
    noinline body: suspend RoutingContext.(Resource) -> Unit,
): Route = head<Resource> { resource ->
    body(resource)
    call.respond(contract.successStatusCode)
}.describeContract(contract)

/**
 * Registers an `OPTIONS` route from an [OptionsEndpointContract].
 *
 * Automatically responds with [OptionsEndpointContract.successStatusCode] and
 * attaches OpenAPI documentation generated from the contract.
 */
inline fun <reified Resource : Any, reified Response : Any> Route.endpoint(
    contract: OptionsEndpointContract<Resource, Response>,
    noinline body: suspend RoutingContext.(Resource) -> Response,
): Route = options<Resource> { resource ->
    val response = body(resource)
    call.respond(contract.successStatusCode, response)
}.describeContract(contract)
