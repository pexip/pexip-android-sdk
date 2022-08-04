@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    includeBuild("plugins")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
    versionCatalogs {
        register("pexipSdk") {
            from("com.pexip.sdk:sdk-catalog:0.7.1")
        }
    }
}

rootProject.name = "pexip-android-sdk"
include(":sample")

val localSettings = file("local.settings.gradle.kts")
if (localSettings.exists()) apply(from = localSettings)
