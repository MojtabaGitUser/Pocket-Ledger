plugins {
    id("folentra.android.library")
}

android {
    namespace = "com.mojtaba.folentra.core.background"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
