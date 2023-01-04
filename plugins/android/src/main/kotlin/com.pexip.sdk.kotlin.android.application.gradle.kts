@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KaptExtension

plugins {
    id("com.pexip.sdk.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.diffplug.spotless")
}

pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
    configure<KaptExtension> {
        correctErrorTypes = true
    }
}

spotless {
    kotlin {
        ratchetFrom = "origin/main"
        target("src/*/kotlin/**/*.kt", "src/*/kotlin/**/*.kts")
        ktlint()
    }
}

android {
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    kotlinOptions {
        jvmTarget = "${compileOptions.targetCompatibility}"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    testImplementation(kotlin("test-junit"))
    androidTestImplementation(kotlin("test-junit"))
}
