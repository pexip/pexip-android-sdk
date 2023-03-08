plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(libs.dokka)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
