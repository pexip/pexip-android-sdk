import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "Pexip SDK core"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=com.pexip.sdk.core.InternalSdkApi")
    }
}
