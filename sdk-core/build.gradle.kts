plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

kotlin.sourceSets.all {
    languageSettings.optIn("com.pexip.sdk.core.InternalSdkApi")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

publishing.publications.withType<MavenPublication> {
    pom.description = "Pexip SDK core"
}
