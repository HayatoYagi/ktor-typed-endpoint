plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":core"))
    implementation(libs.ktor.http)
    implementation(libs.ktor.resources)
    implementation(libs.kotlinx.serialization.json)
}
