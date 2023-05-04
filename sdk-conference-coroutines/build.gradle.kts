plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-conference"))
    api(libs.kotlinx.coroutines.core)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Coroutines support for sdk-conference.")
}
