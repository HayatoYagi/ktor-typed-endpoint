pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ktor-typed-endpoint"

include(":core")
include(":ktor-server")
include(":ktor-client")
include(":sample")
