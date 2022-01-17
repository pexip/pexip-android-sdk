@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.paddock.kotlin.android.library")
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
    kotlinOptions {
        freeCompilerArgs += "-Xopt-in=com.squareup.workflow1.ui.WorkflowUiExperimentalApi"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.accompanist.appcompattheme)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.minidns.hla)
    implementation(libs.minidns.android21)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logginginterceptor)
    implementation(libs.workflow.core.jvm)
    implementation(libs.workflow.ui.compose)

    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
    testImplementation(libs.workflow.testing.jvm)
}
