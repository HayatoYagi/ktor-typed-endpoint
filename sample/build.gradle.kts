plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("io.github.hayatoyagi.ktortyped.sample.SampleAppKt")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":ktor-server"))
    implementation(project(":ktor-client"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.routing.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    ksp(project(":ktor-client-ksp"))
}

ktor {
    fatJar {
        archiveFileName.set("ktor-typed-endpoint-sample.jar")
    }
}
