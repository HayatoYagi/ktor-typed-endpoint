package io.github.hayatoyagi.ktortyped

/**
 * Assigns OpenAPI tags to a route resource and all its child resources.
 *
 * Apply this annotation to a class in your Ktor `@Resource` hierarchy.
 * Tags are **inherited by all child resources**, so annotating a parent class
 * automatically groups all its endpoints together in Swagger UI.
 *
 * Example:
 * ```kotlin
 * @ApiTag("books")
 * @Serializable
 * @Resource("books")
 * class Books(val parent: ApiRoutes = ApiRoutes()) {
 *     // Books.ById and its children automatically inherit the "books" tag
 *     @Serializable
 *     @Resource("{id}")
 *     data class ById(val parent: Books = Books(), val id: String)
 * }
 * ```
 *
 * @property value One or more tag names to assign.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiTag(vararg val value: String)
