plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkConference)

    api(libs.kotlinx.coroutines.core)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("Coroutines support for sdk-conference.")
}
