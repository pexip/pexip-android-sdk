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

    api(libs.androidx.core.ktx)
    api(libs.androidx.media)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Android-specific extensions for sdk-media.")
}
