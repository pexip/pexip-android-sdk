plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-api"))
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Coroutines support for sdk-api."
}
