plugins {
    id("pocketledger.android.library")
}

android {
    namespace = "com.mojtaba.pocketledger.feature.transaction"
}

dependencies {
    testImplementation(libs.junit)
}
