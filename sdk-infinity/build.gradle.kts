plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-core"))
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Pexip Infinity SDK"
}
