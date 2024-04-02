plugins {
    id("com.pexip.sdk.kotlin.multiplatform.publishing")
}

kotlin {
    jvm()
    sourceSets {
        all {
            languageSettings.optIn("com.pexip.sdk.core.InternalSdkApi")
        }
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

publishing.publications.withType<MavenPublication> {
    pom.description = "Pexip SDK core"
}
