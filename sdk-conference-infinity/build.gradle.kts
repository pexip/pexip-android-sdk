plugins {
    id("com.pexip.sdk.kotlin.jvm.publish")
}

dependencies {
    api(project(":sdk-conference"))
    api(project(":sdk-api-infinity"))
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Infinity-based implementation of sdk-conference.")
}
