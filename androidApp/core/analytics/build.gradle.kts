plugins {
    id("pocketledger.android.library")
}

android {
    namespace = "com.mojtaba.pocketledger.core.analytics"
}

dependencies {
    testImplementation(libs.junit)
}