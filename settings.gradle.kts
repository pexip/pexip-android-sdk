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
include(":sdk-catalog")
include(":sdk-conference")
include(":sdk-core")
include(":sdk-infinity")
include(":sdk-infinity-test")
include(":sdk-media")
include(":sdk-media-android")
include(":sdk-media-webrtc")
include(":sdk-media-webrtc-compose")
include(":sdk-registration")

val localSettings = file("local.settings.gradle.kts")
if (localSettings.exists()) apply(from = localSettings)
