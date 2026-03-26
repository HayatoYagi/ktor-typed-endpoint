import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.ksp.api)
    implementation(project(":ktor-client"))
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.hayatoyagi", "ktor-typed-endpoint-ktor-client-ksp", project.version.toString())
    pom {
        name.set("ktor-typed-endpoint-ktor-client-ksp")
        description.set("KSP processor that generates type-safe HttpClient extension functions from ktor-typed-endpoint contracts.")
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
