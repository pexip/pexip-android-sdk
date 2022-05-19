plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkMedia)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("A set of tools to interact with conferences.")
}
