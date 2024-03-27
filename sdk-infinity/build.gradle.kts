plugins {
    id("com.pexip.sdk.kotlin.multiplatform.publishing")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                api(project(":sdk-core"))
                api(libs.kotlinx.serialization.core)
            }
        }
    }
}

publishing.publications.withType<MavenPublication> {
    pom.description = "Pexip Infinity SDK"
}
