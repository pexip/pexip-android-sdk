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
    moduleName.set("Pexip Android SDK")
    includes.from("README.md")
}
