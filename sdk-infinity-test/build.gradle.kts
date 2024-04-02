plugins {
    id("com.pexip.sdk.kotlin.multiplatform.publishing")
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                api(project(":sdk-infinity"))
            }
        }
    }
}

publishing.publications.withType<MavenPublication> {
    pom.description = "Pexip Infinity SDK test utilities"
}
