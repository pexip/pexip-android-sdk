@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.pexip.sdk.kotlin.android.application")
    id("com.pexip.sdk.kotlin.kapt")
    id("com.pexip.sdk.licensee")
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.wire)
}

group = checkNotNull(property("group")) { "group == null." }
version = checkNotNull(property("version")) { "version == null." }

android {
    namespace = "com.pexip.sdk.sample"
    defaultConfig {
        applicationId = checkNotNull(namespace) { "namespace is not set." }
        versionCode = 1
        versionName = version.toString()
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        freeCompilerArgs += "-opt-in=com.squareup.workflow1.ui.WorkflowUiExperimentalApi"
    }
}

hilt {
    enableAggregatingTask = true
}

wire {
    kotlin { }
}

dependencies {
    implementation(project(":sdk-api-coroutines"))
    implementation(project(":sdk-conference-infinity"))
    implementation(project(":sdk-conference-coroutines"))
    implementation(project(":sdk-media-coroutines"))
    implementation(project(":sdk-media-webrtc-compose"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.dagger.hilt.android.runtime)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.minidns.android21)
    implementation(libs.okhttp.logginginterceptor)
    implementation(libs.workflow.core.jvm)
    implementation(libs.workflow.ui.compose)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
