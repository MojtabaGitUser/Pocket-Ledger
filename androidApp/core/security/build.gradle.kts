plugins {
    id("pocketledger.android.library")
}

android {
    namespace = "com.mojtaba.pocketledger.core.security"
}

dependencies {
    implementation(libs.androidx.security.crypto)
    implementation(libs.google.errorprone.annotations)
    implementation(libs.jsr305)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
}
