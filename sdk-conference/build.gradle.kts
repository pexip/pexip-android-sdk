plugins {
    id("com.pexip.sdk.kotlin.jvm.publish")
}

dependencies {
    api(project(":sdk-media"))
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of tools to interact with conferences.")
}