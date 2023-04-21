@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.gradle.android.cache-fix")
}

android {
    compileSdk = internal.Android.compileSdk
    defaultConfig {
        minSdk = internal.Android.minSdk
        targetSdk = internal.Android.targetSdk
        testInstrumentationRunner = internal.Android.testInstrumentationRunner
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = internal.Android.sourceCompatibility
        targetCompatibility = internal.Android.targetCompatibility
    }
    composeOptions {
        kotlinCompilerExtensionVersion = internal.Android.composeCompilerExtensionVersion
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    lint {
        checkDependencies = true
        ignoreTestSources = true
    }
    packaging {
        resources.excludes += "META-INF/{AL2.0,LGPL2.1}"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<Test>().configureEach {
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
}
