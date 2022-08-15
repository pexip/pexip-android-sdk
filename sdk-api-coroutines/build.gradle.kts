import java.net.URL

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

publishing.publications.withType<MavenPublication>().configureEach {
    pom.description.set("Coroutines support for sdk-api.")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        externalDocumentationLink {
            url.set(URL("https://kotlin.github.io/kotlinx.coroutines/"))
        }
    }
}
