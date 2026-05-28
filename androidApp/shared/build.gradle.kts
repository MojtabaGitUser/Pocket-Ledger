plugins {
    id("pocketledger.kotlin.multiplatform")
}

kotlin {
    android {
        namespace = "com.mojtaba.pocketledger.shared"
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
