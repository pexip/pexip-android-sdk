plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":kotlin"))
    implementation(libs.android)
    implementation(libs.cachefix)
    implementation(libs.kotlin)
    implementation(libs.licensee)
}
