@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    includeBuild("plugins")
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}

rootProject.name = "pexip-android-sdk"
include(":sample")
include(":sdk-api")
include(":sdk-api-infinity")
include(":sdk-api-coroutines")
include(":sdk-catalog")
include(":sdk-conference")
include(":sdk-conference-infinity")
include(":sdk-conference-coroutines")
include(":sdk-media")
include(":sdk-media-android")
include(":sdk-media-coroutines")
include(":sdk-media-webrtc")
include(":sdk-media-webrtc-compose")
include(":sdk-registration")
include(":sdk-registration-infinity")
include(":sdk-registration-coroutines")

val localSettings = file("local.settings.gradle.kts")
if (localSettings.exists()) apply(from = localSettings)
