plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    explicitApi()
    jvmToolchain(8)
}

dependencies {
    testImplementation(kotlin("test-junit"))
}

tasks.withType<Test>().configureEach {
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    systemProperty("kotlinx.coroutines.stacktrace.recovery", false)
}
