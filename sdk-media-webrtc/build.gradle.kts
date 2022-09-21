plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    namespace = "com.pexip.sdk.media.webrtc"
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkMediaAndroid)

    api(libs.pexip.webrtc)

    implementation(libs.androidx.core)
    implementation(libs.androidx.media)

    testImplementation(libs.okio)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("WebRTC-based implementation of sdk-media.")
}
