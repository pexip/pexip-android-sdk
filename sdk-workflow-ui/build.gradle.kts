plugins {
    id("com.pexip.paddock.kotlin.android.library")
}

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
}

dependencies {
    api(projects.sdkWorkflowCore)
    api(libs.androidx.compose.ui)
}
