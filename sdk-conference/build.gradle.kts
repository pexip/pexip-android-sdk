plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

kotlin.sourceSets.all {
    languageSettings.optIn("com.pexip.sdk.core.InternalSdkApi")
    languageSettings.optIn("com.pexip.sdk.core.ExperimentalSdkApi")
}

dependencies {
    api(project(":sdk-api"))
    api(project(":sdk-media"))
    testImplementation(project(":sdk-infinity-test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

publishing.publications.withType<MavenPublication> {
    pom.description = "A set of tools to interact with conferences."
}
