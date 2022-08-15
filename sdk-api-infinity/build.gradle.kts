@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()
}

dependencies {
    api(projects.sdkApi)

    api(libs.okhttp)
    api(libs.okio)

    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.minidns.hla)

    testImplementation(libs.okhttp.mockwebserver)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A fluent wrapper for Pexip Infinity Client REST API.")
}
