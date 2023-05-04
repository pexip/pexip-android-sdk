plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.pexip.sdk.android.application"
            implementationClass = "com.pexip.sdk.AndroidApplicationPlugin"
        }
        register("androidLibrary") {
            id = "com.pexip.sdk.android.library"
            implementationClass = "com.pexip.sdk.AndroidLibraryPlugin"
        }
        register("kotlinAndroidApplication") {
            id = "com.pexip.sdk.kotlin.android.application"
            implementationClass = "com.pexip.sdk.KotlinAndroidApplicationPlugin"
        }
        register("kotlinAndroidLibrary") {
            id = "com.pexip.sdk.kotlin.android.library"
            implementationClass = "com.pexip.sdk.KotlinAndroidLibraryPlugin"
        }
        register("kotlinAndroidLibraryPublishing") {
            id = "com.pexip.sdk.kotlin.android.library.publishing"
            implementationClass = "com.pexip.sdk.KotlinAndroidLibraryPublishingPlugin"
        }
    }
}

dependencies {
    implementation(project(":kotlin"))
    implementation(libs.android)
    implementation(libs.cachefix)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
