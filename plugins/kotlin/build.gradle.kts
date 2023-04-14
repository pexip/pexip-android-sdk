plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.dokka)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
