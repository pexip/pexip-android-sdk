plugins {
    id("com.pexip.sdk.kotlin.dokka")
    alias(libs.plugins.kotlinx.binarycompatibilityvalidator)
    alias(libs.plugins.spotless)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    id("com.pexip.sdk.publishing") apply false
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
        val customRuleSets = listOf(libs.ktlint.compose.map { "${it.module}:${it.version}" }.get())
        // Spotless doesn't always seem to pick up changes in .editorconfig, so mirror them here
        val override = buildMap {
            this["max_line_length"] = 100
            this["ktlint_code_style"] = "intellij_idea"
            this["ktlint_function_naming_ignore_when_annotated_with"] = "Composable"
        }
        ktlint(ktlintVersion)
            .customRuleSets(customRuleSets)
            .editorConfigOverride(override)
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
        leadingTabsToSpaces()
        endWithNewline()
    }
}

dependencies {
    dokka(project(":sdk-api"))
    dokka(project(":sdk-conference"))
    dokka(project(":sdk-core"))
    dokka(project(":sdk-infinity"))
    dokka(project(":sdk-infinity-test"))
    dokka(project(":sdk-media"))
    dokka(project(":sdk-media-android"))
    dokka(project(":sdk-media-webrtc"))
    dokka(project(":sdk-media-webrtc-compose"))
    dokka(project(":sdk-registration"))
}

dokka {
    moduleName.set(project.name)
    dokkaPublications.html {
        includes.from("README.md")
    }
}
