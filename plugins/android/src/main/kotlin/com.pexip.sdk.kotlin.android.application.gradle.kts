@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KaptExtension

plugins {
    id("com.pexip.sdk.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
}

pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
    configure<KaptExtension> {
        correctErrorTypes = true
    }
}

android {
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0-rc01"
    }
    kotlinOptions {
        jvmTarget = "${compileOptions.targetCompatibility}"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    sourceSets.configureEach {
        java.srcDirs("src/$name/kotlin")
    }
}

dependencies {
    testImplementation(kotlin("test-junit"))
    androidTestImplementation(kotlin("test-junit"))
}
