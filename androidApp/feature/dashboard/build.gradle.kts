plugins {
    id("pocketledger.android.library")
}

android {
    namespace = "com.mojtaba.pocketledger.feature.dashboard"
}

dependencies {
    implementation(project(":core:data"))

    testImplementation(libs.junit)
}
