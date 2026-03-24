package io.github.hayatoyagi.ktortyped

import io.ktor.http.HttpStatusCode

/**
 * Base class for all typed HTTP endpoint contracts.
 *
 * An endpoint contract binds together the Ktor [Resource] type, the HTTP method,
 * the request/response body types, and the success [HttpStatusCode] into a single object.
 * This allows route registration and OpenAPI generation to be driven from one place.
 *
 * @param Resource The Ktor `@Resource`-annotated class that defines the route path and parameters.
 * @param successStatusCode The HTTP status code returned on a successful response.
 */
abstract class EndpointContract<Resource : Any>(
    val successStatusCode: HttpStatusCode,
)

/**
 * Contract for a `GET` endpoint.
 *
 * @param Resource The Ktor `@Resource`-annotated class for the route.
 * @param Response The response body type.
 */
abstract class GetEndpointContract<Resource : Any, Response : Any>(
    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
) : EndpointContract<Resource>(successStatusCode = successStatusCode)

/**
 * Contract for a `POST` endpoint.
 *
 * @param Resource The Ktor `@Resource`-annotated class for the route.
 * @param Request The request body type.
 * @param Response The response body type.
 */
abstract class PostEndpointContract<Resource : Any, Request : Any, Response : Any>(
    successStatusCode: HttpStatusCode = HttpStatusCode.Created,
) : EndpointContract<Resource>(successStatusCode = successStatusCode)

/**
 * Contract for a `PUT` endpoint.
 *
 * @param Resource The Ktor `@Resource`-annotated class for the route.
 * @param Request The request body type.
 * @param Response The response body type.
 */
abstract class PutEndpointContract<Resource : Any, Request : Any, Response : Any>(
    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
) : EndpointContract<Resource>(successStatusCode = successStatusCode)

/**
 * Contract for a `PATCH` endpoint.
 *
 * @param Resource The Ktor `@Resource`-annotated class for the route.
 * @param Request The request body type.
 * @param Response The response body type.
 */
abstract class PatchEndpointContract<Resource : Any, Request : Any, Response : Any>(
    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
) : EndpointContract<Resource>(successStatusCode = successStatusCode)

/**
 * Contract for a `DELETE` endpoint.
 *
 * @param Resource The Ktor `@Resource`-annotated class for the route.
 * @param Response The response body type.
 */
abstract class DeleteEndpointContract<Resource : Any, Response : Any>(
    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
) : EndpointContract<Resource>(successStatusCode = successStatusCode)

/**
 * Contract for a `HEAD` endpoint.
 *
 * HEAD responses have no body — only status code and headers.
 *
 * @param Resource The Ktor `@Resource`-annotated class for the route.
 */
abstract class HeadEndpointContract<Resource : Any>(
    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
) : EndpointContract<Resource>(successStatusCode = successStatusCode)

/**
 * Contract for an `OPTIONS` endpoint.
 *
 * @param Resource The Ktor `@Resource`-annotated class for the route.
 * @param Response The response body type.
 */
abstract class OptionsEndpointContract<Resource : Any, Response : Any>(
    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
) : EndpointContract<Resource>(successStatusCode = successStatusCode)
