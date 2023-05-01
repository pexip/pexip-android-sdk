@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.sdk.kotlin.android.library.publishing")
}

android {
    namespace = "com.pexip.sdk.media.webrtc.compose"
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":sdk-media-webrtc"))
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Compose support for sdk-media-webrtc.")
}
