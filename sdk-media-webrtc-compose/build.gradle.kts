plugins {
    id("com.pexip.paddock.kotlin.android.library")
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
