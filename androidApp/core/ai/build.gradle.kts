plugins {
    id("folentra.android.library")
}

android {
    namespace = "com.mojtaba.folentra.core.ai"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:featureflags"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.mlkit.genai.prompt)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
