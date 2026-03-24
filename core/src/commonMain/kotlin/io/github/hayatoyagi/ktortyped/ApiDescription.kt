package io.github.hayatoyagi.ktortyped

/**
 * Provides an OpenAPI description for a request or response model.
 *
 * Apply this annotation to data classes used as request or response bodies in endpoint contracts.
 * The description is automatically included in the generated OpenAPI spec:
 * - For request models: `requestBody.description`
 * - For response models: `responses.<status>.description`
 *
 * Example:
 * ```kotlin
 * @ApiDescription(
 *     """
 *     Creates a new book.
 *
 *     Validation rules:
 *     - `title` must not be blank.
 *     - `authorId` must reference an existing author.
 *     """
 * )
 * @Serializable
 * data class CreateBookRequest(
 *     val title: String,
 *     val authorId: String,
 * )
 * ```
 *
 * @property value The description text. Supports markdown formatting.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiDescription(val value: String)
