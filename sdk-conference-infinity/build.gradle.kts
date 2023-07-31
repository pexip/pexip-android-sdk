plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-conference"))
    api(project(":sdk-api-infinity"))
    testImplementation(libs.kotlinx.coroutines.test)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Infinity-based implementation of sdk-conference.")
}
