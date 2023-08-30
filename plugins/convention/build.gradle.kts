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
        register("kotlinAndroid") {
            id = "com.pexip.sdk.kotlin.android"
            implementationClass = "com.pexip.sdk.KotlinAndroidPlugin"
        }
        register("kotlinAndroidLibraryPublishing") {
            id = "com.pexip.sdk.kotlin.android.library.publishing"
            implementationClass = "com.pexip.sdk.KotlinAndroidLibraryPublishingPlugin"
        }
        register("kotlinDokka") {
            id = "com.pexip.sdk.kotlin.dokka"
            implementationClass = "com.pexip.sdk.KotlinDokkaPlugin"
        }
        register("kotlinJvm") {
            id = "com.pexip.sdk.kotlin.jvm"
            implementationClass = "com.pexip.sdk.KotlinJvmPlugin"
        }
        register("kotlinJvmPublishing") {
            id = "com.pexip.sdk.kotlin.jvm.publishing"
            implementationClass = "com.pexip.sdk.KotlinJvmPublishingPlugin"
        }
        register("licensee") {
            id = "com.pexip.sdk.licensee"
            implementationClass = "com.pexip.sdk.LicenseePlugin"
        }
        register("publishing") {
            id = "com.pexip.sdk.publishing"
            implementationClass = "com.pexip.sdk.PublishingPlugin"
        }
    }
}

dependencies {
    implementation(libs.android)
    implementation(libs.dokka)
    implementation(libs.dokka.base)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
