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
    testImplementation(libs.okio)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("WebRTC-based implementation of sdk-media.")
}
