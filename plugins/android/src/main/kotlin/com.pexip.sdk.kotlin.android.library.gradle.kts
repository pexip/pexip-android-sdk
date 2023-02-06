@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.sdk.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    kotlinOptions {
        jvmTarget = "${compileOptions.targetCompatibility}"
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    testImplementation(kotlin("test-junit"))
    androidTestImplementation(kotlin("test-junit"))
}

tasks.withType<Test>().configureEach {
    systemProperty("kotlinx.coroutines.stacktrace.recovery", false)
}
