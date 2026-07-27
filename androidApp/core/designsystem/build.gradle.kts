plugins {
    id("folentra.android.library")
    id("folentra.android.compose")
}

android {
    namespace = "com.mojtaba.folentra.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
}
