import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":core"))
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.routing.openapi)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.kotlinx.serialization.json)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.hayatoyagi", "ktor-typed-endpoint-ktor-server", project.version.toString())
    pom {
        name.set("ktor-typed-endpoint-ktor-server")
        description.set("Type-safe HTTP endpoint contracts for Ktor — Ktor server integration with routing and OpenAPI generation.")
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
