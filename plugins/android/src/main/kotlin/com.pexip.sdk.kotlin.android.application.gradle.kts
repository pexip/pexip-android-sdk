@file:Suppress("UnstableApiUsage")

plugins {
    id("com.pexip.sdk.android.application")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    androidTestImplementation(kotlin("test-junit"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
}

tasks.withType<Test>().configureEach {
    systemProperty("kotlinx.coroutines.stacktrace.recovery", false)
}
