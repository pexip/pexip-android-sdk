plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(libs.dokka)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
