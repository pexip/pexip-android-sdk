plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(project(":kotlin"))
    implementation(libs.android)
    implementation(libs.cachefix)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
