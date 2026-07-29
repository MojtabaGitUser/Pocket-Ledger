package com.mojtaba.folentra.observability

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseCrashlyticsSmokeTest {
    @Test
    fun sendsSyntheticNonFatalOnlyWhenExplicitlyRequested() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assumeTrue(
            "Pass -e firebaseCrashlyticsSmoke true to send the synthetic event.",
            InstrumentationRegistry.getArguments().getString(ARGUMENT_NAME).toBoolean(),
        )
        val context = instrumentation.targetContext
        assumeTrue(
            "A valid google-services.json is required.",
            context.resources.getIdentifier("google_app_id", "string", context.packageName) != 0,
        )

        val crashlytics = FirebaseCrashlytics.getInstance()
        try {
            crashlytics.setCrashlyticsCollectionEnabled(true)
            crashlytics.setCustomKey("event", EVENT_NAME)
            crashlytics.setCustomKey("build_variant", "debug-smoke-test")
            crashlytics.recordException(
                IllegalStateException("Synthetic Folentra Crashlytics non-fatal validation event."),
            )
            crashlytics.sendUnsentReports()
        } finally {
            crashlytics.setCrashlyticsCollectionEnabled(false)
        }
    }

    private companion object {
        const val ARGUMENT_NAME = "firebaseCrashlyticsSmoke"
        const val EVENT_NAME = "firebase_non_fatal_smoke_probe"
    }
}
