plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":sdk-infinity"))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    api(libs.minidns.hla)
    api(libs.okhttp)
    api(libs.okio)
    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.serialization.json.okio)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "A set of common classes and interfaces to build APIs."
}
