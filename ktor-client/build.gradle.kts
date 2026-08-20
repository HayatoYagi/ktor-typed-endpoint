import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvm()
    android {
        namespace = "io.github.hayatoyagi.ktortyped.client"
        compileSdk = 36
        minSdk = 21
    }
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    macosArm64()
    macosX64()
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()
    linuxArm64()
    linuxX64()
    mingwX64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()
    js { browser(); nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(libs.ktor.client.core)
            api(libs.ktor.client.resources)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":ktor-server"))
            implementation(libs.ktor.server.test.host)
            implementation(libs.ktor.server.resources)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.status.pages)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.hayatoyagi", "ktor-typed-endpoint-ktor-client", project.version.toString())
    pom {
        name.set("ktor-typed-endpoint-ktor-client")
        description.set("Type-safe HTTP endpoint contracts for Ktor — Ktor client integration with typed error responses.")
        url.set("https://github.com/HayatoYagi/ktor-typed-endpoint")
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        developers {
            developer {
                id.set("HayatoYagi")
                name.set("Hayato Yagi")
                url.set("https://github.com/HayatoYagi")
            }
        }
        scm {
            url.set("https://github.com/HayatoYagi/ktor-typed-endpoint")
            connection.set("scm:git:git://github.com/HayatoYagi/ktor-typed-endpoint.git")
            developerConnection.set("scm:git:ssh://git@github.com/HayatoYagi/ktor-typed-endpoint.git")
        }
    }
}
