plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description =
        "A set of classes and interfaces to help with establishing a media connection."
}
