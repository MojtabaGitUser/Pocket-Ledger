plugins {
    id("pocketledger.android.application")
    id("pocketledger.android.compose")
    alias(libs.plugins.paparazzi)
}

tasks.withType<Test>().configureEach {
    maxParallelForks = 1
    val isPaparazziTaskRequested = gradle.startParameter.taskNames.any { taskName ->
        taskName.contains("Paparazzi", ignoreCase = true) ||
            taskName.contains("AdaptiveScreenshots", ignoreCase = true)
    }
    if (!isPaparazziTaskRequested) {
        exclude("**/screenshot/**")
    }
}

android {
    namespace = "com.mojtaba.pocketledger"

    val releaseStoreFile = providers.gradleProperty("POCKET_LEDGER_RELEASE_STORE_FILE")
    val releaseStorePassword = providers.gradleProperty("POCKET_LEDGER_RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = providers.gradleProperty("POCKET_LEDGER_RELEASE_KEY_ALIAS")
    val releaseKeyPassword = providers.gradleProperty("POCKET_LEDGER_RELEASE_KEY_PASSWORD")
    val hasReleaseSigningConfig =
        releaseStoreFile.isPresent &&
            releaseStorePassword.isPresent &&
            releaseKeyAlias.isPresent &&
            releaseKeyPassword.isPresent

    defaultConfig {
        applicationId = "com.mojtaba.pocketledger"
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = file(releaseStoreFile.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            buildConfigField("String", "APP_ENV", "\"debug\"")
            buildConfigField("Boolean", "IS_INTERNAL_BUILD", "true")
            buildConfigField("Boolean", "LOGGING_ENABLED", "true")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "APP_ENV", "\"release\"")
            buildConfigField("Boolean", "IS_INTERNAL_BUILD", "false")
            buildConfigField("Boolean", "LOGGING_ENABLED", "false")
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isProfileable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "APP_ENV", "\"benchmark\"")
            buildConfigField("Boolean", "IS_INTERNAL_BUILD", "true")
            buildConfigField("Boolean", "LOGGING_ENABLED", "false")
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

apply(plugin = "androidx.baselineprofile")

dependencies {
    add("baselineProfile", project(":macrobenchmark"))
    add("benchmarkImplementation", project(":core:testing"))
    implementation(project(":core:ai"))
    implementation(project(":core:background"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:featureflags"))
    implementation(project(":core:security"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:search"))
    implementation(project(":feature:transaction"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.window)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(project(":core:testing"))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
