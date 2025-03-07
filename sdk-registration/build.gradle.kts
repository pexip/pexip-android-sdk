plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

kotlin.sourceSets.all {
    languageSettings.optIn("com.pexip.sdk.core.InternalSdkApi")
}

dependencies {
    api(project(":sdk-api"))
    testImplementation(project(":sdk-infinity-test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

mavenPublishing.pom {
    description = "A set of tools to interact with registrations."
}
