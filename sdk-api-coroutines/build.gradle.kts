plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

dependencies {
    api(projects.sdkApi)

    api(libs.kotlinx.coroutines.core)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("Coroutines support for sdk-api.")
}
