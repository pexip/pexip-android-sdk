@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.pexip.sdk.kotlin.android.application")
    alias(libs.plugins.kotlin.parcelize)
}

android {
    defaultConfig {
        applicationId = "com.pexip.sdk.sample"
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
        freeCompilerArgs += "-Xopt-in=com.squareup.workflow1.ui.WorkflowUiExperimentalApi"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.accompanist.appcompattheme)
    implementation(libs.okhttp.logginginterceptor)
    implementation(libs.pexip.sdk.api.coroutines)
    implementation(libs.pexip.sdk.conference.infinity)
    implementation(libs.pexip.sdk.conference.coroutines)
    implementation(libs.pexip.sdk.media.coroutines)
    implementation(libs.pexip.sdk.media.webrtc.compose)
    implementation(libs.workflow.core.jvm)
    implementation(libs.workflow.ui.compose)
}
