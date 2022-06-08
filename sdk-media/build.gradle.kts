plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("A set of classes and interfaces to help with establishing a media connection.")
}
