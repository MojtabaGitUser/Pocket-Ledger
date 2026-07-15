plugins {
    id("pocketledger.android.application")
    id("pocketledger.android.compose")
    alias(libs.plugins.paparazzi)

    id("com.android.application")

    // Add the Google services Gradle plugin
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.crashlytics)

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

fun releaseProperty(name: String) = providers.gradleProperty(name).orElse(providers.environmentVariable(name))

val pocketLedgerVersionCode = releaseProperty("POCKET_LEDGER_VERSION_CODE").map { rawValue ->
    require(rawValue.matches(Regex("[1-9][0-9]{0,8}"))) {
        "POCKET_LEDGER_VERSION_CODE must be a positive integer with at most 9 digits."
    }
    rawValue.toInt()
}
val pocketLedgerVersionName = releaseProperty("POCKET_LEDGER_VERSION_NAME").map { rawValue ->
    require(rawValue.matches(Regex("""[0-9]+\.[0-9]+\.[0-9]+([-.][A-Za-z0-9]+)*"""))) {
        "POCKET_LEDGER_VERSION_NAME must use semantic format such as 1.0.0 or 1.0.0-rc.1."
    }
    require(rawValue.length <= 32) { "POCKET_LEDGER_VERSION_NAME must be 32 characters or fewer." }
    rawValue
}

val releaseStoreFile = releaseProperty("POCKET_LEDGER_RELEASE_STORE_FILE")
val releaseStorePassword = releaseProperty("POCKET_LEDGER_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = releaseProperty("POCKET_LEDGER_RELEASE_KEY_ALIAS")
val releaseKeyPassword = releaseProperty("POCKET_LEDGER_RELEASE_KEY_PASSWORD")
val releaseSigningValues = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
)
val releaseSigningValueCount = releaseSigningValues.count { it.isPresent }
val hasReleaseSigningConfig = releaseSigningValueCount == releaseSigningValues.size
val requireReleaseSigning = releaseProperty("POCKET_LEDGER_REQUIRE_RELEASE_SIGNING")
    .map { it.equals("true", ignoreCase = true) }
    .getOrElse(false)

if (releaseSigningValueCount in 1 until releaseSigningValues.size) {
    throw GradleException(
        "Release signing is partially configured. Provide all POCKET_LEDGER_RELEASE_* values " +
            "or remove them for unsigned validation builds."
    )
}
if (requireReleaseSigning && !hasReleaseSigningConfig) {
    throw GradleException(
        "POCKET_LEDGER_REQUIRE_RELEASE_SIGNING=true requires POCKET_LEDGER_RELEASE_STORE_FILE, " +
            "POCKET_LEDGER_RELEASE_STORE_PASSWORD, POCKET_LEDGER_RELEASE_KEY_ALIAS, and " +
            "POCKET_LEDGER_RELEASE_KEY_PASSWORD."
    )
}

android {
    namespace = "com.mojtaba.pocketledger"

    defaultConfig {
        applicationId = "com.mojtaba.pocketledger"
        targetSdk = 36
        versionCode = pocketLedgerVersionCode.get()
        versionName = pocketLedgerVersionName.get()
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                val signingStore = file(releaseStoreFile.get())
                if (!signingStore.isFile) {
                    throw GradleException("Release signing store file does not exist: ${signingStore.absolutePath}")
                }
                storeFile = signingStore
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
            buildConfigField("Boolean", "CRASH_REPORTING_ENABLED", "false")
            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = "false"
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "APP_ENV", "\"release\"")
            buildConfigField("Boolean", "IS_INTERNAL_BUILD", "false")
            buildConfigField("Boolean", "LOGGING_ENABLED", "false")
            buildConfigField("Boolean", "CRASH_REPORTING_ENABLED", "true")
            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = "true"
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
            buildConfigField("Boolean", "CRASH_REPORTING_ENABLED", "false")
            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = "false"
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

tasks.register("validateReleaseSigning") {
    group = "verification"
    description = "Fails unless release signing is fully configured for release-ready APK/AAB builds."
    doLast {
        if (!hasReleaseSigningConfig) {
            throw GradleException(
                "Release signing is not configured. Set POCKET_LEDGER_RELEASE_STORE_FILE, " +
                    "POCKET_LEDGER_RELEASE_STORE_PASSWORD, POCKET_LEDGER_RELEASE_KEY_ALIAS, and " +
                    "POCKET_LEDGER_RELEASE_KEY_PASSWORD."
            )
        }
    }
}

apply(plugin = "androidx.baselineprofile")

dependencies {
    add("baselineProfile", project(":macrobenchmark"))
    add("benchmarkImplementation", project(":core:testing"))
    implementation(project(":core:ai"))
    implementation(project(":core:analytics"))
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
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.google.play.integrity)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.window)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    compileOnly(libs.google.errorprone.annotations)
    compileOnly(libs.jsr305)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.work.testing)
    testImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(project(":core:testing"))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.leakcanary.android)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)


}
