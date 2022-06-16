@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.pexip.sdk.kotlin.android.application")
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.kapt)
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

hilt {
    enableAggregatingTask = true
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.accompanist.appcompattheme)
    implementation(libs.dagger.hilt.android.runtime)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.minidns.android21)
    implementation(libs.okhttp.logginginterceptor)
    implementation(libs.workflow.core.jvm)
    implementation(libs.workflow.ui.compose)
    implementation(pexipSdk.api.coroutines)
    implementation(pexipSdk.conference.infinity)
    implementation(pexipSdk.conference.coroutines)
    implementation(pexipSdk.media.coroutines)
    implementation(pexipSdk.media.webrtc.compose)
}
