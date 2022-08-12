plugins {
    id("com.pexip.paddock.publish")
    `version-catalog`
}

group = checkNotNull(property("group")) { "group == null." }
version = checkNotNull(property("version")) { "version == null." }

catalog {
    versionCatalog {
        val aliases = listOf(
            "api",
            "api-coroutines",
            "api-infinity",
            "conference",
            "conference-coroutines",
            "conference-infinity",
            "registration",
            "registration-coroutines",
            "registration-infinity",
            "media",
            "media-coroutines",
            "media-webrtc",
            "media-webrtc-compose"
        )
        aliases.forEach { alias -> library(alias, "$group:sdk-$alias:$version") }
    }
}

val javadocJar by tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(file("README.md"))
}

val sourcesJar by tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(file("README.md"))
}

publishing {
    publications {
        named<MavenPublication>("release") {
            from(components["versionCatalog"])
            artifact(javadocJar)
            artifact(sourcesJar)
            pom.description.set("Gradle Version Catalog for Pexip Android SDK")
        }
    }
}
