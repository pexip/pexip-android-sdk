plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

dependencies {
    api(projects.sdkMedia)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("A set of tools to interact with conferences.")
}
