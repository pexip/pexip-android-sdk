import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.pexip.sdk.kotlin.android.library.publishing")
}

configure<LibraryExtension> {
    namespace = "com.pexip.sdk.media.android"
}

dependencies {
    api(project(":sdk-media"))
    api(libs.androidx.core.ktx)
    api(libs.androidx.media)
}

mavenPublishing.pom {
    description = "Android-specific extensions for sdk-media."
}
