import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
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
    js { browser(); nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(libs.ktor.client.core)
            api(libs.ktor.client.resources)
        }
    }
}

android {
    namespace = "io.github.hayatoyagi.ktortyped.client"
    compileSdk = 36
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.hayatoyagi", "ktor-typed-endpoint-ktor-client", project.version.toString())
    pom {
        name.set("ktor-typed-endpoint-ktor-client")
        description.set("Type-safe HTTP client extensions for ktor-typed-endpoint contracts.")
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
