plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("kotlinDokka") {
            id = "com.pexip.sdk.kotlin.dokka"
            implementationClass = "com.pexip.sdk.KotlinDokkaPlugin"
        }
        register("kotlinJvm") {
            id = "com.pexip.sdk.kotlin.jvm"
            implementationClass = "com.pexip.sdk.KotlinJvmPlugin"
        }
        register("kotlinJvmPublish") {
            id = "com.pexip.sdk.kotlin.jvm.publish"
            implementationClass = "com.pexip.sdk.KotlinJvmPublishingPlugin"
        }
        register("kotlinKapt") {
            id = "com.pexip.sdk.kotlin.kapt"
            implementationClass = "com.pexip.sdk.KotlinKaptPlugin"
        }
        register("licensee") {
            id = "com.pexip.sdk.licensee"
            implementationClass = "com.pexip.sdk.LicenseePlugin"
        }
        register("publishing") {
            id = "com.pexip.sdk.publishing"
            implementationClass = "com.pexip.sdk.PublishingPlugin"
        }
    }
}

dependencies {
    implementation(libs.dokka)
    implementation(libs.dokka.base)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
