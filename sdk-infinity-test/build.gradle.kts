plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-infinity"))
}

publishing.publications.withType<MavenPublication> {
    pom.description = "Pexip Infinity SDK test utilities"
}
