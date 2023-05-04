@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":sdk-api"))
    api(libs.minidns.hla)
    api(libs.okhttp)
    api(libs.okio)
    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.serialization.json.okio)
    testImplementation(libs.okhttp.mockwebserver)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A fluent wrapper for Pexip Infinity Client REST API.")
}
