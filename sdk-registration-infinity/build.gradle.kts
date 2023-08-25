plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-registration"))
    api(project(":sdk-api-infinity"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Infinity-based implementation of sdk-registration.")
}
