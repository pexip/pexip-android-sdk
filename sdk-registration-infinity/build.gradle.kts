plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-registration"))
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Infinity-based implementation of sdk-registration."
}
