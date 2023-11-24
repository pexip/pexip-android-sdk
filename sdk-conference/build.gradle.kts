plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":sdk-api"))
    api(project(":sdk-media"))
    implementation(libs.kotlinx.serialization.json.okio)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "A set of tools to interact with conferences."
}
