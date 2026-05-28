plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mojtaba.pocketledger"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

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
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
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
            isDebuggable = true
        }

        release {
            isMinifyEnabled = false
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
