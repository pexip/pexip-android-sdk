plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
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

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("Compose support for sdk-media-webrtc.")
}
