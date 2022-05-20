# Pexip Android SDK

## Sample app

Sample app is available at [GitHub](https://github.com/pexip/pexip-android-sdk). By default, it
depends on stable release from Maven Central.

For development purposes, you can utilize Gradle's dependency substitution mechanism and use this
project with the sample app.

- Open `pexip-android-sdk` project
- Modify or create `local.settings.gradle.kts`
- Paste the following contents there:

```kotlin
// Replace with the absolute path
includeBuild("/path/to/paddock/sdk") {
    dependencySubstitution {
        substitute(module("com.pexip.sdk:sdk-api"))
            .using(project(":sdk-api"))
        substitute(module("com.pexip.sdk:sdk-api-coroutines"))
            .using(project(":sdk-api-coroutines"))
        substitute(module("com.pexip.sdk:sdk-api-infinity"))
            .using(project(":sdk-api-infinity"))
        substitute(module("com.pexip.sdk:sdk-conference"))
            .using(project(":sdk-conference"))
        substitute(module("com.pexip.sdk:sdk-conference-coroutines"))
            .using(project(":sdk-conference-coroutines"))
        substitute(module("com.pexip.sdk:sdk-conference-infinity"))
            .using(project(":sdk-conference-infinity"))
        substitute(module("com.pexip.sdk:sdk-media"))
            .using(project(":sdk-media"))
        substitute(module("com.pexip.sdk:sdk-media-coroutines"))
            .using(project(":sdk-media-coroutines"))
        substitute(module("com.pexip.sdk:sdk-media-webrtc"))
            .using(project(":sdk-media-webrtc"))
        substitute(module("com.pexip.sdk:sdk-media-webrtc-compose"))
            .using(project(":sdk-media-webrtc-compose"))
    }
}
```
