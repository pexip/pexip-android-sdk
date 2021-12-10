plugins {
    id("com.pexip.paddock.kotlin.android.library")
}

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
}

dependencies {
    api(libs.androidx.compose.runtime)
}
