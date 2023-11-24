plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.json)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Pexip Infinity SDK"
}
