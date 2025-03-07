plugins {
    id("com.pexip.sdk.kotlin.multiplatform.publishing")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            api(project(":sdk-core"))
            api(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            api(libs.minidns.hla)
        }
    }
}

mavenPublishing.pom {
    description = "Pexip Infinity SDK"
}
