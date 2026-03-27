package io.github.hayatoyagi.ktortyped.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.github.hayatoyagi.ktortyped.DeleteEndpointContract
import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.PatchEndpointContract
import io.github.hayatoyagi.ktortyped.PostEndpointContract
import io.github.hayatoyagi.ktortyped.PutEndpointContract

/**
 * Sends a `GET` request described by [contract] for the given [resource].
 *
 * The response is deserialized to [Response] automatically.
 * Intended to be called from KSP-generated client functions — use those instead of calling this directly.
 */
suspend inline fun <reified Resource : Any, reified Response : Any> HttpClient.request(
    contract: GetEndpointContract<Resource, Response>,
    resource: Resource,
): Response = get(resource).body()

/**
 * Sends a `POST` request described by [contract] with [body] for the given [resource].
 */
suspend inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> HttpClient.request(
    contract: PostEndpointContract<Resource, Request, Response>,
    resource: Resource,
    body: Request,
): Response = post(resource) { setBody(body) }.body()

/**
 * Sends a `PUT` request described by [contract] with [body] for the given [resource].
 */
suspend inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> HttpClient.request(
    contract: PutEndpointContract<Resource, Request, Response>,
    resource: Resource,
    body: Request,
): Response = put(resource) { setBody(body) }.body()

/**
 * Sends a `PATCH` request described by [contract] with [body] for the given [resource].
 */
suspend inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> HttpClient.request(
    contract: PatchEndpointContract<Resource, Request, Response>,
    resource: Resource,
    body: Request,
): Response = patch(resource) { setBody(body) }.body()

/**
 * Sends a `DELETE` request described by [contract] for the given [resource].
 */
suspend inline fun <reified Resource : Any, reified Response : Any> HttpClient.request(
    contract: DeleteEndpointContract<Resource, Response>,
    resource: Resource,
): Response = delete(resource).body()
