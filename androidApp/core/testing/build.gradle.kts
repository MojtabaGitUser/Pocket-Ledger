plugins {
    id("pocketledger.android.library")
}

android {
    namespace = "com.mojtaba.pocketledger.core.testing"
}

dependencies {
    api(project(":core:data"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
