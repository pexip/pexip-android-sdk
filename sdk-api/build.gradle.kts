plugins {
    id("com.pexip.sdk.kotlin.jvm.publish")
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of common classes and interfaces to build APIs.")
}
