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
    packagingOptions {
        resources.excludes += internal.Android.resourcesExcludes
    }
}

tasks.withType<Test>().configureEach {
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
}
