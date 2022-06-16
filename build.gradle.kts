@file:Suppress("DSL_SCOPE_VIOLATION")

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.android)
        classpath(libs.dokka)
        classpath(libs.kotlin)
    }
}

plugins {
    alias(libs.plugins.dokka)
}

group = checkNotNull(property("group")) { "group == null." }
version = checkNotNull(property("version")) { "version == null." }

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

tasks.dokkaHtmlMultiModule.configure {
    includes.from("README.md")
    val calendar = java.util.Calendar.getInstance(
        java.util.TimeZone.getTimeZone("UTC"),
        java.util.Locale.ENGLISH
    )
    val footerMessage = "${calendar.get(java.util.Calendar.YEAR)} Pexip® AS, All rights reserved."
    val m = mapOf(
        "org.jetbrains.dokka.base.DokkaBase" to """{
            |"footerMessage": "$footerMessage",
            |"customAssets": ["${file("dokka/pexip.svg")}"],
            |"customStyleSheets": ["${file("dokka/logo-styles.css")}"]
            |}""".trimMargin()
    )
    pluginsMapConfiguration.set(m)
}
