# Pexip Android SDK

![main](https://github.com/pexip/pexip-android-sdk/actions/workflows/main.yml/badge.svg)

**Pexip Android SDK** is a collection of libraries for
self-hosted [Pexip Infinity](https://docs.pexip.com/admin/admin_intro.htm) installations that
enables customers to build bespoke applications for Android or add Pexip to existing mobile
experiences and workflows.

## Using in your projects

### Gradle

Make sure that you have `mavenCentral()` in the list of repositories:

```kotlin
repositories {
    mavenCentral()
}
```

And add modules that you need:

```kotlin
dependencies {
    // A fluent wrapper for Infinity Client REST API
    implementation("com.pexip.sdk:sdk-api:0.17.4")
    // A set of tools for interacting with an Infinity conference
    implementation("com.pexip.sdk:sdk-conference:0.17.4")
    // A set of tools for interacting with an Infinity registration
    implementation("com.pexip.sdk:sdk-registration:0.17.4")
    // A `MediaConnection` implementation build on top of WebRTC
    implementation("com.pexip.sdk:sdk-media-webrtc:0.17.4")
    // A composable that wraps SurfaceViewRenderer
    implementation("com.pexip.sdk:sdk-media-webrtc-compose:0.17.4")
}
```

We also publish
a [Version Catalog](https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog)
that can be consumed as follows:

```kotlin
// settings.gradle.kts
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        register("pexipSdk") {
            from("com.pexip.sdk:sdk-catalog:0.17.4")
        }
    }
}
// build.gradle.kts
dependencies {
    implementation(pexipSdk.api.infinity)
}
```

Snapshot builds are
also [available](https://s01.oss.sonatype.org/content/repositories/snapshots/com/pexip/sdk/) and can
be configured as follows:

```kotlin
repositories {
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}
```

## Requirements

* JDK 17
* Android SDK 21+
* Pexip Infinity 29+

### Android Java API desugaring

This library uses types from `java.time.*` and thus requires the projects to have a `minSdk` that
has these APIs available or
use [library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring).

## WebRTC versions

| SDK version | WebRTC Milestone |
|-------------|------------------|
| 0.17.4+     | 137              |
| 0.17.1+     | 134              |
| 0.17.0+     | 129              |
| 0.14.0+     | 119              |
| 0.13.0+     | 114              |
| 0.12.0+     | 110              |
| 0.10.0+     | 104              |
| 0.7.0+      | 102              |
| 0.1.0+      | 96               |

## Documentation

- [Recipes](https://github.com/pexip/pexip-android-sdk/blob/main/docs/recipes.md)
- [API reference](https://pexip.github.io/pexip-android-sdk/)
