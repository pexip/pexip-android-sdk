plugins {
    id("com.pexip.paddock.kotlin.android.library")
}

android {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(projects.sdkMedia)
    api(projects.sdkWebrtc)

    implementation(libs.androidx.core)

    testImplementation(libs.okio)
}
