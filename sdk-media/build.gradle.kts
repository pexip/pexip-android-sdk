plugins {
    id("com.pexip.sdk.kotlin.jvm.publish")
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of classes and interfaces to help with establishing a media connection.")
}