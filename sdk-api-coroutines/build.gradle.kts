plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-api"))
    api(libs.kotlinx.coroutines.core)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Coroutines support for sdk-api.")
}
