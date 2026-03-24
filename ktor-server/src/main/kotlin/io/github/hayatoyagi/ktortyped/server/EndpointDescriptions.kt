package io.github.hayatoyagi.ktortyped.server

import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import io.github.hayatoyagi.ktortyped.ApiDescription
import io.github.hayatoyagi.ktortyped.ApiTag
import io.github.hayatoyagi.ktortyped.DeleteEndpointContract
import io.github.hayatoyagi.ktortyped.GetEndpointContract
import io.github.hayatoyagi.ktortyped.HeadEndpointContract
import io.github.hayatoyagi.ktortyped.OptionsEndpointContract
import io.github.hayatoyagi.ktortyped.PatchEndpointContract
import io.github.hayatoyagi.ktortyped.PostEndpointContract
import io.github.hayatoyagi.ktortyped.PutEndpointContract
import kotlin.reflect.KClass

@PublishedApi internal fun KClass<*>.apiDescription(): String? =
    annotations.firstNotNullOfOrNull { (it as? ApiDescription)?.value?.trimIndent() }

@PublishedApi internal fun KClass<*>.collectApiTags(): List<String> {
    val tags = LinkedHashSet<String>()
    var current: Class<*>? = this.java
    while (current != null) {
        current.kotlin.annotations.filterIsInstance<ApiTag>().forEach { tags.addAll(it.value) }
        current = runCatching { current.getDeclaredField("parent") }.getOrNull()?.type
    }
    return tags.toList()
}

@OptIn(ExperimentalKtorApi::class)
@PublishedApi internal inline fun <reified Resource : Any, reified Response : Any> Route.describeContract(
    contract: GetEndpointContract<Resource, Response>,
): Route = describe {
    Resource::class.collectApiTags().forEach { tag(it) }
    val responseDescription = Response::class.apiDescription()
    responses {
        contract.successStatusCode {
            schema = jsonSchema<Response>()
            responseDescription?.let { description = it }
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
@PublishedApi internal inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> Route.describeContract(
    contract: PostEndpointContract<Resource, Request, Response>,
): Route = describe {
    Resource::class.collectApiTags().forEach { tag(it) }
    val requestDescription = Request::class.apiDescription()
    val responseDescription = Response::class.apiDescription()
    requestBody {
        schema = jsonSchema<Request>()
        requestDescription?.let { description = it }
    }
    responses {
        contract.successStatusCode {
            schema = jsonSchema<Response>()
            responseDescription?.let { description = it }
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
@PublishedApi internal inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> Route.describeContract(
    contract: PutEndpointContract<Resource, Request, Response>,
): Route = describe {
    Resource::class.collectApiTags().forEach { tag(it) }
    val requestDescription = Request::class.apiDescription()
    val responseDescription = Response::class.apiDescription()
    requestBody {
        schema = jsonSchema<Request>()
        requestDescription?.let { description = it }
    }
    responses {
        contract.successStatusCode {
            schema = jsonSchema<Response>()
            responseDescription?.let { description = it }
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
@PublishedApi internal inline fun <reified Resource : Any, reified Request : Any, reified Response : Any> Route.describeContract(
    contract: PatchEndpointContract<Resource, Request, Response>,
): Route = describe {
    Resource::class.collectApiTags().forEach { tag(it) }
    val requestDescription = Request::class.apiDescription()
    val responseDescription = Response::class.apiDescription()
    requestBody {
        schema = jsonSchema<Request>()
        requestDescription?.let { description = it }
    }
    responses {
        contract.successStatusCode {
            schema = jsonSchema<Response>()
            responseDescription?.let { description = it }
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
@PublishedApi internal inline fun <reified Resource : Any, reified Response : Any> Route.describeContract(
    contract: DeleteEndpointContract<Resource, Response>,
): Route = describe {
    Resource::class.collectApiTags().forEach { tag(it) }
    val responseDescription = Response::class.apiDescription()
    responses {
        contract.successStatusCode {
            schema = jsonSchema<Response>()
            responseDescription?.let { description = it }
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
@PublishedApi internal inline fun <reified Resource : Any> Route.describeContract(
    contract: HeadEndpointContract<Resource>,
): Route = describe {
    Resource::class.collectApiTags().forEach { tag(it) }
    responses {
        contract.successStatusCode { }
    }
}

@OptIn(ExperimentalKtorApi::class)
@PublishedApi internal inline fun <reified Resource : Any, reified Response : Any> Route.describeContract(
    contract: OptionsEndpointContract<Resource, Response>,
): Route = describe {
    Resource::class.collectApiTags().forEach { tag(it) }
    val responseDescription = Response::class.apiDescription()
    responses {
        contract.successStatusCode {
            schema = jsonSchema<Response>()
            responseDescription?.let { description = it }
        }
    }
}
