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

    api(libs.pexip.webrtc)

    implementation(libs.androidx.core)

    testImplementation(libs.okio)
}
