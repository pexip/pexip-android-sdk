plugins {
    id("com.pexip.sdk.kotlin.android.library.publish")
}

android {
    namespace = "com.pexip.sdk.media.webrtc"
}

dependencies {
    api(project(":sdk-media-android"))
    api(libs.webrtc)
    testImplementation(libs.okio)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("WebRTC-based implementation of sdk-media.")
}
