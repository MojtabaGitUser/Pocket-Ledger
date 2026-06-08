plugins {
    id("pocketledger.android.library")
}

android {
    namespace = "com.mojtaba.pocketledger.core.featureflags"
}

dependencies {
    testImplementation(libs.junit)
}
