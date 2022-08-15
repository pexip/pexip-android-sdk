plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of common classes and interfaces to build APIs.")
}
