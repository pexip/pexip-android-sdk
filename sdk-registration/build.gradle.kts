plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "A set of tools to interact with registrations."
}
