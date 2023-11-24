plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":sdk-conference"))
    api(project(":sdk-api-infinity"))
    implementation(libs.kotlinx.serialization.json.okio)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Infinity-based implementation of sdk-conference."
}
