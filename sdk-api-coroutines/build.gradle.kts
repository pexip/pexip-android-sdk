plugins {
    id("com.pexip.paddock.kotlin.jvm")
}

kotlin {
    explicitApi()
}

dependencies {
    api(projects.sdkApi)

    api(libs.kotlinx.coroutines.core)
}
