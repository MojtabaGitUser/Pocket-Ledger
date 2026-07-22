plugins {
    id("folentra.android.library")
}

android {
    namespace = "com.mojtaba.folentra.core.testing"
}

dependencies {
    api(project(":core:background"))
    api(project(":core:data"))
    api(project(":core:featureflags"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
