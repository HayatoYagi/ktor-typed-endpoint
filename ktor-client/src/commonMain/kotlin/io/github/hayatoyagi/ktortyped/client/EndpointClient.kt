package io.github.hayatoyagi.ktortyped.client

import io.github.hayatoyagi.ktortyped.DeleteEndpointContract
import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.OptionsEndpointContract
import io.github.hayatoyagi.ktortyped.PatchEndpointContract
import io.github.hayatoyagi.ktortyped.PostEndpointContract
import io.github.hayatoyagi.ktortyped.PutEndpointContract
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.options
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException

/**
 * Sends a `GET` request described by [contract] for the given [resource].
 *
 * The response type is inferred from the contract, so no explicit type annotation is needed.
 * Throws [EndpointErrorException] if the response status does not match
 * [io.github.hayatoyagi.ktortyped.EndpointContract.successStatusCode].
 */
suspend inline fun <reified Resource : Any, reified Response : Any, reified Error : Any> HttpClient.request(
    contract: GetEndpointContract<Resource, Response, Error>,
    resource: Resource,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): Response = get(resource, builder).toContractResponse<Response, Error>(contract.successStatusCode)

/**
 * Sends a `POST` request described by [contract] with [request] for the given [resource].
 *
 * Throws [EndpointErrorException] if the response status does not match
 * [io.github.hayatoyagi.ktortyped.EndpointContract.successStatusCode].
 */
suspend inline fun <
    reified Resource : Any,
    reified Request : Any,
    reified Response : Any,
    reified Error : Any,
    > HttpClient.request(
    contract: PostEndpointContract<Resource, Request, Response, Error>,
    resource: Resource,
    request: Request,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): Response = post(resource) {
    contentType(ContentType.Application.Json)
    setBody(request)
    builder()
}.toContractResponse<Response, Error>(contract.successStatusCode)

/**
 * Sends a `PUT` request described by [contract] with [request] for the given [resource].
 *
 * Throws [EndpointErrorException] if the response status does not match
 * [io.github.hayatoyagi.ktortyped.EndpointContract.successStatusCode].
 */
suspend inline fun <
    reified Resource : Any,
    reified Request : Any,
    reified Response : Any,
    reified Error : Any,
    > HttpClient.request(
    contract: PutEndpointContract<Resource, Request, Response, Error>,
    resource: Resource,
    request: Request,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): Response = put(resource) {
    contentType(ContentType.Application.Json)
    setBody(request)
    builder()
}.toContractResponse<Response, Error>(contract.successStatusCode)

/**
 * Sends a `PATCH` request described by [contract] with [request] for the given [resource].
 *
 * Throws [EndpointErrorException] if the response status does not match
 * [io.github.hayatoyagi.ktortyped.EndpointContract.successStatusCode].
 */
suspend inline fun <
    reified Resource : Any,
    reified Request : Any,
    reified Response : Any,
    reified Error : Any,
    > HttpClient.request(
    contract: PatchEndpointContract<Resource, Request, Response, Error>,
    resource: Resource,
    request: Request,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): Response = patch(resource) {
    contentType(ContentType.Application.Json)
    setBody(request)
    builder()
}.toContractResponse<Response, Error>(contract.successStatusCode)

/**
 * Sends a `DELETE` request described by [contract] for the given [resource].
 *
 * Throws [EndpointErrorException] if the response status does not match
 * [io.github.hayatoyagi.ktortyped.EndpointContract.successStatusCode].
 */
suspend inline fun <reified Resource : Any, reified Response : Any, reified Error : Any> HttpClient.request(
    contract: DeleteEndpointContract<Resource, Response, Error>,
    resource: Resource,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): Response = delete(resource, builder).toContractResponse<Response, Error>(contract.successStatusCode)

/**
 * Sends an `OPTIONS` request described by [contract] for the given [resource].
 *
 * Throws [EndpointErrorException] if the response status does not match
 * [io.github.hayatoyagi.ktortyped.EndpointContract.successStatusCode].
 */
suspend inline fun <reified Resource : Any, reified Response : Any, reified Error : Any> HttpClient.request(
    contract: OptionsEndpointContract<Resource, Response, Error>,
    resource: Resource,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): Response = options(resource, builder).toContractResponse<Response, Error>(contract.successStatusCode)

@PublishedApi
internal suspend inline fun <reified Response : Any, reified Error : Any> HttpResponse.toContractResponse(
    successStatusCode: HttpStatusCode,
): Response {
    if (status == successStatusCode) return body()
    throw EndpointErrorException(status = status, errorBody = readErrorBodyOrNull<Error>())
}

@PublishedApi
internal suspend inline fun <reified Error : Any> HttpResponse.readErrorBodyOrNull(): Error? =
    try {
        body<Error>()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
