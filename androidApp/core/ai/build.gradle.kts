plugins {
    id("pocketledger.android.library")
}

android {
    namespace = "com.mojtaba.pocketledger.core.ai"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:featureflags"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
