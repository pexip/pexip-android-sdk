@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.paddock.kotlin.android.application")
}

android {
    defaultConfig {
        applicationId = "com.pexip.sdk.video.sample"
        // Remove if we ever decide to publish sample as an artifact
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
    }
    kotlinOptions {
        freeCompilerArgs += "-Xopt-in=com.pexip.sdk.workflow.core.ExperimentalWorkflowApi"
        freeCompilerArgs += "-Xopt-in=com.pexip.sdk.workflow.ui.ExperimentalWorkflowUiApi"
    }
}

dependencies {
    implementation(projects.sdkVideo)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material)
    implementation(libs.accompanist.appcompattheme)
}
