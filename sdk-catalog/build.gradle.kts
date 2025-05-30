import com.vanniktech.maven.publish.VersionCatalog

plugins {
    `version-catalog`
    id("com.pexip.sdk.publishing")
}

catalog {
    versionCatalog {
        val prefix = "sdk-"
        rootProject.childProjects.values.asSequence()
            .filter { it != project && it.name.startsWith(prefix) }
            .forEach {
                val artifact = it.name
                val alias = artifact.removePrefix(prefix)
                val group = group.toString()
                val version = version.toString()
                library(alias, group, artifact).version(version)
            }
    }
}

val javadocJar by tasks.register<Jar>("javadocJar") {
    archiveClassifier = "javadoc"
    from(file("README.md"))
}

val sourcesJar by tasks.register<Jar>("sourcesJar") {
    archiveClassifier = "sources"
    from(file("README.md"))
}

mavenPublishing {
    configure(VersionCatalog())
    pom {
        description = "Gradle Version Catalog for Pexip Android SDK"
    }
}

publishing.publications.named<MavenPublication>("maven") {
    artifact(javadocJar)
    artifact(sourcesJar)
}
