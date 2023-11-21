plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Pexip Infinity SDK"
}
