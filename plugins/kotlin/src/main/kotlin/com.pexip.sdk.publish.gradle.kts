plugins {
    `maven-publish`
    signing
}

group = checkNotNull(property("group")) { "group == null." }
version = checkNotNull(property("version")) { "version == null." }

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
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set(artifactId)
            url.set("https://github.com/pexip/pexip-android-sdk")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    name.set("Dzmitry Rymarau")
                    email.set("dzmitry.rymarau@pexip.com")
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
                connection.set("scm:git:git://github.com/pexip/pexip-android-sdk.git")
                developerConnection.set("scm:git:ssh://github.com:pexip/pexip-android-sdk.git")
                url.set("https://github.com/pexip/pexip-android-sdk")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    if (providers.environmentVariable("CI").isPresent) {
        sign(publishing.publications)
    }
}
