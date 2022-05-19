@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
    alias(libs.plugins.kotlin.serialization)
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkApi)

    api(libs.okhttp)

    implementation(libs.okhttp.sse)
    implementation(libs.okio)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.minidns.hla)
    implementation(libs.minidns.android21)

    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("A fluent wrapper for Pexip Infinity Client REST API.")
}
