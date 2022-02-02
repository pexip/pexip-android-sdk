@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.paddock.kotlin.android.library")
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.androidx.startup.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.minidns.hla)
    implementation(libs.minidns.android21)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logginginterceptor)

    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
}
