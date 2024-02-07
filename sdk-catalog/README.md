# Module sdk-catalog

Gradle Version Catalog for Pexip Android SDK

## Using in your projects

### Gradle

```kotlin
// settings.gradle.kts
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        register("pexipSdk") {
            from("com.pexip.sdk:sdk-catalog:0.14.1")
        }
    }
}
// build.gradle.kts
dependencies {
    implementation(pexipSdk.api.infinity)
}
```
