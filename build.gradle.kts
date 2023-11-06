plugins {
    id("com.pexip.sdk.kotlin.dokka")
    alias(libs.plugins.kotlinx.binarycompatibilityvalidator)
    alias(libs.plugins.spotless)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
}

group = checkNotNull(property("group")) { "group == null." }
version = checkNotNull(property("version")) { "version == null." }

apiValidation {
    ignoredProjects += "sample"
}

spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    kotlin {
        ratchetFrom = "origin/main"
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(ktlintVersion)
        licenseHeaderFile("LICENSE_HEADER")
    }
    kotlinGradle {
        ratchetFrom = "origin/main"
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(ktlintVersion)
    }
    format("misc") {
        target(".gitignore", "*.md")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}
