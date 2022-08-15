plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

dependencies {
    api(projects.sdkApiInfinity)
    api(projects.sdkConference)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Infinity-based implementation of sdk-conference.")
}
