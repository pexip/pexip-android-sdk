plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of tools to interact with registrations.")
}
