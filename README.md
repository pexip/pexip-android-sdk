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
    implementation("com.pexip.sdk:sdk-api-infinity:0.4.1")
    // A set of tools for interacting with an Infinity conference
    implementation("com.pexip.sdk:sdk-conference-infinity:0.4.1")
    // A `MediaConnection` implementation build on top of WebRTC
    implementation("com.pexip.sdk:sdk-media-webrtc:0.4.1")
    // A set of extensions that add coroutines support for Infinity Client REST API
    implementation("com.pexip.sdk:sdk-api-coroutines:0.4.1")
    // A set of extensions that add coroutines support for Conference object
    implementation("com.pexip.sdk:sdk-conference-coroutines:0.4.1")
    // A set of extensions that add coroutines support for MediaConnection object
    implementation("com.pexip.sdk:sdk-media-coroutines:0.4.1")
    // A composable that wraps org.webrtc.SurfaceViewRenderer
    implementation("com.pexip.sdk:sdk-media-webrtc-compose:0.4.1")
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
            from("com.pexip.sdk:sdk-catalog:0.4.1")
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

## Documentation

- [Recipes](docs/recipes.md)
- [API reference](https://pexip.github.io/pexip-android-sdk/)
