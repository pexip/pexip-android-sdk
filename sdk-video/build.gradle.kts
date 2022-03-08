@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.paddock.kotlin.android.library")
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(libs.okhttp)

    implementation(libs.androidx.startup.runtime)
    implementation(libs.okhttp.sse)
    implementation(libs.okio)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.minidns.hla)
    implementation(libs.minidns.android21)
    implementation(libs.pexip.libwebrtc.core)

    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
}
