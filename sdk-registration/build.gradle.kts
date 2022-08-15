plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of tools to interact with registrations.")
}
