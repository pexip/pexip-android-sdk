plugins {
    id("com.pexip.sdk.kotlin.jvm.publish")
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of tools to interact with registrations.")
}
