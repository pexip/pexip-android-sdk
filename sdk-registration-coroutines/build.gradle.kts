import java.net.URL

plugins {
    id("com.pexip.paddock.kotlin.jvm.publish")
}

kotlin {
    explicitApi()
}

dependencies {
    api(projects.sdkRegistration)

    api(libs.kotlinx.coroutines.core)
}

publishing.publications.named<MavenPublication>("release") {
    pom.description.set("Coroutines support for sdk-registration.")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        externalDocumentationLink {
            url.set(URL("https://kotlin.github.io/kotlinx.coroutines/"))
        }
    }
}
