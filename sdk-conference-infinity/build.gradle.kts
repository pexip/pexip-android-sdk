plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkApiInfinity)
    api(projects.sdkConference)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("Infinity-based implementation of sdk-conference.")
}
