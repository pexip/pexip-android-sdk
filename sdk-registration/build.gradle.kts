import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.pexip.sdk.kotlin.jvm.publishing")
}

dependencies {
    api(project(":sdk-api"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description = "A set of tools to interact with registrations."
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=com.pexip.sdk.core.InternalSdkApi")
    }
}
