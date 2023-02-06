plugins {
    id("com.pexip.sdk.kotlin.android.library.publish")
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
    pom.description.set("Android-specific extensions for sdk-media.")
}
