plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-api"))
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "A fluent wrapper for Pexip Infinity Client REST API."
}
