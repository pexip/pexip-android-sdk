plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

dependencies {
    api(projects.sdkConference)

    api(libs.kotlinx.coroutines.core)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("Coroutines support for sdk-conference.")
}
