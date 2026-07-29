plugins {
    id("folentra.android.application")
    id("folentra.android.compose")
    alias(libs.plugins.paparazzi)

    id("com.android.application")

    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.google.crashlytics) apply false

}

val googleServicesFile = layout.projectDirectory.file("google-services.json").asFile
val googleServicesContents = googleServicesFile
    .takeIf { it.isFile }
    ?.readText()
    .orEmpty()
fun hasFirebaseClient(packageName: String): Boolean =
    Regex(""""package_name"\s*:\s*"${Regex.escape(packageName)}"""")
        .containsMatchIn(googleServicesContents)

val firebaseConfiguredForFolentra = googleServicesFile.isFile &&
    hasFirebaseClient("com.mojtaba.folentra") &&
    hasFirebaseClient("com.mojtaba.folentra.debug")
if (firebaseConfiguredForFolentra) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
} else {
    logger.warn(
        "Firebase is disabled: download a google-services.json containing both " +
            "com.mojtaba.folentra and com.mojtaba.folentra.debug clients."
    )
}

tasks.register("validateFirebaseConfiguration") {
    group = "verification"
    description = "Validates that google-services.json contains both Folentra Android clients."
    doLast {
        check(firebaseConfiguredForFolentra) {
            "A valid app/google-services.json containing com.mojtaba.folentra and " +
                "com.mojtaba.folentra.debug is required for Firebase-enabled builds."
        }
    }
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

val folentraVersionCode = releaseProperty("FOLENTRA_VERSION_CODE").map { rawValue ->
    require(rawValue.matches(Regex("[1-9][0-9]{0,8}"))) {
        "FOLENTRA_VERSION_CODE must be a positive integer with at most 9 digits."
    }
    rawValue.toInt()
}
val folentraVersionName = releaseProperty("FOLENTRA_VERSION_NAME").map { rawValue ->
    require(rawValue.matches(Regex("""[0-9]+\.[0-9]+\.[0-9]+([-.][A-Za-z0-9]+)*"""))) {
        "FOLENTRA_VERSION_NAME must use semantic format such as 1.0.0 or 1.0.0-rc.1."
    }
    require(rawValue.length <= 32) { "FOLENTRA_VERSION_NAME must be 32 characters or fewer." }
    rawValue
}

val releaseStoreFile = releaseProperty("FOLENTRA_RELEASE_STORE_FILE")
val releaseStorePassword = releaseProperty("FOLENTRA_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = releaseProperty("FOLENTRA_RELEASE_KEY_ALIAS")
val releaseKeyPassword = releaseProperty("FOLENTRA_RELEASE_KEY_PASSWORD")
val releaseSigningValues = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
)
val releaseSigningValueCount = releaseSigningValues.count { it.isPresent }
val hasReleaseSigningConfig = releaseSigningValueCount == releaseSigningValues.size
val requireReleaseSigning = releaseProperty("FOLENTRA_REQUIRE_RELEASE_SIGNING")
    .map { it.equals("true", ignoreCase = true) }
    .getOrElse(false)

if (releaseSigningValueCount in 1 until releaseSigningValues.size) {
    throw GradleException(
        "Release signing is partially configured. Provide all FOLENTRA_RELEASE_* values " +
            "or remove them for unsigned validation builds."
    )
}
if (requireReleaseSigning && !hasReleaseSigningConfig) {
    throw GradleException(
        "FOLENTRA_REQUIRE_RELEASE_SIGNING=true requires FOLENTRA_RELEASE_STORE_FILE, " +
            "FOLENTRA_RELEASE_STORE_PASSWORD, FOLENTRA_RELEASE_KEY_ALIAS, and " +
            "FOLENTRA_RELEASE_KEY_PASSWORD."
    )
}

android {
    namespace = "com.mojtaba.folentra"

    defaultConfig {
        applicationId = "com.mojtaba.folentra"
        targetSdk = 36
        versionCode = folentraVersionCode.get()
        versionName = folentraVersionName.get()
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
            buildConfigField("Boolean", "CRASH_REPORTING_ENABLED", firebaseConfiguredForFolentra.toString())
            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = firebaseConfiguredForFolentra.toString()
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
                "Release signing is not configured. Set FOLENTRA_RELEASE_STORE_FILE, " +
                    "FOLENTRA_RELEASE_STORE_PASSWORD, FOLENTRA_RELEASE_KEY_ALIAS, and " +
                    "FOLENTRA_RELEASE_KEY_PASSWORD."
            )
        }
    }
}

val validateBackupAndDeviceTransferRules by tasks.registering {
    group = "verification"
    description = "Validates the deny-by-default Android backup and device-transfer contract."
    val manifest = layout.projectDirectory.file("src/main/AndroidManifest.xml")
    val legacyRules = layout.projectDirectory.file("src/main/res/xml/backup_rules.xml")
    val extractionRules = layout.projectDirectory.file("src/main/res/xml/data_extraction_rules.xml")
    inputs.files(manifest, legacyRules, extractionRules)
    doLast {
        val manifestText = manifest.asFile.readText()
        require("android:allowBackup=\"true\"" in manifestText)
        require("android:fullBackupContent=\"@xml/backup_rules\"" in manifestText)
        require("android:dataExtractionRules=\"@xml/data_extraction_rules\"" in manifestText)
        val protectedDomains = listOf("root", "file", "database", "sharedpref", "external")
        fun String.requireDenyByDefault(scope: String) {
            protectedDomains.forEach { domain ->
                require(Regex("""<exclude\s+domain=\"$domain\"\s+path=\"\.\"\s*/>""").containsMatchIn(this)) {
                    "$scope must exclude the complete '$domain' domain."
                }
            }
            require("folentra.db" in this) { "$scope must identify the ledger database." }
            require("folentra_sensitive_prefs.xml" in this) { "$scope must identify encrypted sensitive preferences." }
        }
        legacyRules.asFile.readText().requireDenyByDefault("Pre-Android 12 backup rules")
        val extractionText = extractionRules.asFile.readText()
        fun section(name: String): String = Regex("<$name>([\\s\\S]*?)</$name>")
            .find(extractionText)?.groupValues?.get(1) ?: error("Missing <$name> in data extraction rules.")
        section("cloud-backup").requireDenyByDefault("Android 12+ cloud backup rules")
        section("device-transfer").requireDenyByDefault("Android 12+ device-transfer rules")
    }
}

tasks.named("check").configure { dependsOn(validateBackupAndDeviceTransferRules) }
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
    implementation(libs.androidx.compose.runtime.tracing)
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
    testImplementation(project(":shared"))
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
