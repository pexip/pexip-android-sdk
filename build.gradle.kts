@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinx.binarycompatibilityvalidator)
    alias(libs.plugins.spotless)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

group = checkNotNull(property("group")) { "group == null." }
version = checkNotNull(property("version")) { "version == null." }

apiValidation {
    ignoredProjects += "sample"
}

spotless {
    kotlin {
        ratchetFrom = "origin/main"
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
        licenseHeaderFile("LICENSE_HEADER")
    }
    kotlinGradle {
        ratchetFrom = "origin/main"
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
    }
    format("misc") {
        target(".gitignore", "*.md")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

tasks.dokkaHtmlMultiModule.configure {
    includes.from("README.md")
    val calendar = java.util.Calendar.getInstance(
        java.util.TimeZone.getTimeZone("UTC"),
        java.util.Locale.ENGLISH,
    )
    val footerMessage = "${calendar.get(java.util.Calendar.YEAR)} Pexip AS, All rights reserved."
    val m = mapOf(
        "org.jetbrains.dokka.base.DokkaBase" to """{
            |"footerMessage": "$footerMessage",
            |"customAssets": ["${file("dokka/pexip.svg")}"],
            |"customStyleSheets": ["${file("dokka/logo-styles.css")}"]
            |}
        """.trimMargin(),
    )
    pluginsMapConfiguration.set(m)
}
