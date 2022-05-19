plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkMedia)

    api(libs.pexip.webrtc)

    implementation(libs.androidx.core)

    testImplementation(libs.okio)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("WebRTC-based implementation of sdk-media.")
}
