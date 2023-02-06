plugins {
    id("org.jetbrains.dokka")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        includes.from("README.md")
        externalDocumentationLink("https://kotlin.github.io/kotlinx.coroutines/")
    }
    val timeZone = java.util.TimeZone.getTimeZone("UTC")
    val locale = java.util.Locale.ENGLISH
    val calendar = java.util.Calendar.getInstance(timeZone, locale)
    val year = calendar.get(java.util.Calendar.YEAR)
    val footerMessage = "$year Pexip® AS, All rights reserved."
    val m = mapOf(
        "org.jetbrains.dokka.base.DokkaBase" to """{
        |"footerMessage": "$footerMessage",
        |"separateInheritedMembers": true
        |}
        """.trimMargin(),
    )
    pluginsMapConfiguration.set(m)
}

pluginManager.withPlugin("com.android.library") {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            noAndroidSdkLink.set(false)
        }
    }
}
