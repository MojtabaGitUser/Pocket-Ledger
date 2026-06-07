plugins {
    id("pocketledger.android.library")
    id("pocketledger.android.compose")
}

android {
    namespace = "com.mojtaba.pocketledger.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
}
