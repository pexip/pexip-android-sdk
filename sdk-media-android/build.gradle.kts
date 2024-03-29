plugins {
    id("com.pexip.sdk.kotlin.android.library.publishing")
}

android {
    namespace = "com.pexip.sdk.media.android"
}

dependencies {
    api(project(":sdk-media"))
    api(libs.androidx.core.ktx)
    api(libs.androidx.media)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Android-specific extensions for sdk-media."
}
