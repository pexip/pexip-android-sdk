plugins {
    id("com.pexip.sdk.kotlin.android.library.publishing")
    alias(libs.plugins.kotlin.compose)
}

android.namespace = "com.pexip.sdk.media.webrtc.compose"

dependencies {
    api(project(":sdk-media-webrtc"))
    api(libs.androidx.compose.ui)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling)
}

mavenPublishing.pom {
    description = "Compose support for sdk-media-webrtc."
}
