@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.sdk.kotlin.android.library")
    id("com.pexip.sdk.kotlin.dokka")
    id("com.pexip.sdk.licensee")
    id("com.pexip.sdk.publish")
}

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
