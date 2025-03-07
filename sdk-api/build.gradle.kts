plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
    alias(libs.plugins.kotlin.serialization)
}

kotlin.sourceSets.all {
    languageSettings.optIn("com.pexip.sdk.core.InternalSdkApi")
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
    testImplementation(project(":sdk-infinity-test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.okhttp.tls)
    testImplementation(libs.turbine)
}

mavenPublishing.pom {
    description = "A set of common classes and interfaces to build APIs."
}
