plugins {
    id("folentra.kotlin.multiplatform")
}

kotlin {
    android {
        namespace = "com.mojtaba.folentra.shared"
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
