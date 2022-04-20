@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    includeBuild("../plugin")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
includeBuild("../libwebrtc")

rootProject.name = "sdk"
include(":sdk-api")
include(":sdk-api-infinity")
include(":sdk-api-coroutines")
include(":sdk-conference")
include(":sdk-conference-infinity")
include(":sdk-conference-coroutines")
include(":sdk-media")
include(":sdk-media-coroutines")
include(":sdk-media-webrtc")
include(":sdk-media-webrtc-coroutines")
include(":sdk-video-sample")
