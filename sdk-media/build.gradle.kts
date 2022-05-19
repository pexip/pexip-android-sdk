plugins {
    id("com.pexip.paddock.kotlin.android.library.publish")
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(libs.androidx.annotation)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("A set of classes and interfaces to help with establishing a media connection.")
}
