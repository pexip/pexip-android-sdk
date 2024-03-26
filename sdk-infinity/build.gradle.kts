plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":sdk-core"))
    api(libs.kotlinx.serialization.core)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Pexip Infinity SDK"
}
