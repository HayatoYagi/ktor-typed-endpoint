plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.mavenPublish) apply false
}

// Skip signing when no signing key is configured (e.g. local development or publishToMavenLocal)
subprojects {
    afterEvaluate {
        extensions.findByType<org.gradle.plugins.signing.SigningExtension>()?.apply {
            isRequired = providers.gradleProperty("signingInMemoryKey").isPresent
        }
    }
}
