import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.http)
        }
    }
}

android {
    namespace = "io.github.hayatoyagi.ktortyped"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.hayatoyagi", "ktor-typed-endpoint-core", project.version.toString())
    pom {
        name.set("ktor-typed-endpoint-core")
        description.set("Type-safe HTTP endpoint contracts for Ktor — core contract definitions and annotations.")
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
