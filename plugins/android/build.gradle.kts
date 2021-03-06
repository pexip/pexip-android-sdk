plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(libs.android)
    implementation(libs.cachefix)
    implementation(libs.kotlin)
    implementation(libs.ktlint)
}
