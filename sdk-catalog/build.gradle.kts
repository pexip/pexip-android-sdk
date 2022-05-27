plugins {
    `version-catalog`
    `maven-publish`
    signing
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
    repositories {
        maven {
            name = "MavenCentral"
            val repositoryUrl = when (version.toString().endsWith("SNAPSHOT")) {
                true -> "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else -> "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            }
            setUrl(repositoryUrl)
            credentials(PasswordCredentials::class)
        }
    }
    publications {
        register<MavenPublication>("release") {
            from(components["versionCatalog"])
            artifact(javadocJar)
            artifact(sourcesJar)
            pom {
                name.set(artifactId)
                description.set("Gradle Version Catalog for Pexip Android SDK")
                url.set("https://github.com/pexip/paddock")
                licenses {
                    license {
                        name.set("The Apache License, Version 1.1")
                        url.set("https://www.apache.org/licenses/LICENSE-1.1.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Dmitry Rymarev")
                        email.set("dmitry@pexip.com")
                        organization.set("Pexip")
                        organizationUrl.set("https://www.pexip.com")
                    }
                    developer {
                        name.set("Thomas Pettersen")
                        email.set("thomas.pettersen@pexip.com")
                        organization.set("Pexip")
                        organizationUrl.set("https://www.pexip.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/pexip/paddock.git")
                    developerConnection.set("scm:git:ssh://github.com:pexip/paddock.git")
                    url.set("https://github.com/pexip/paddock")
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["release"])
}
