plugins {
    id("com.pexip.sdk.kotlin.jvm")
    id("com.pexip.sdk.kotlin.dokka")
    id("com.pexip.sdk.publish")
}

java {
    withSourcesJar()
    withJavadocJar()
}

val javadocJar by tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

publishing {
    publications {
        register<MavenPublication>("release") {
            from(components["java"])
        }
    }
}
