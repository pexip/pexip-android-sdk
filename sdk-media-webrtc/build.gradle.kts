plugins {
    id("com.pexip.sdk.kotlin.android.library.publishing")
}

android {
    namespace = "com.pexip.sdk.media.webrtc"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(project(":sdk-media-android"))
    api(libs.webrtc)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okio)
}

mavenPublishing.pom {
    description = "WebRTC-based implementation of sdk-media."
}
