plugins {
    id("com.pexip.sdk.kotlin.jvm.publish")
}

dependencies {
    api(project(":sdk-media"))
    api(libs.kotlinx.coroutines.core)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Coroutines support for sdk-media.")
}