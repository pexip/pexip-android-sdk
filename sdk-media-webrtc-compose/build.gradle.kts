plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    buildFeatures.compose = true
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkMediaWebrtc)

    api(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Compose support for sdk-media-webrtc.")
}
