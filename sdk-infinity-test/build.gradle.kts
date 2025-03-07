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

mavenPublishing.pom {
    description = "Pexip Infinity SDK test utilities"
}
