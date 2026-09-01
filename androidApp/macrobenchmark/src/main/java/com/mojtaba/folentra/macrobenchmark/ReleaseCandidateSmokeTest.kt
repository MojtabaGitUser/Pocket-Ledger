package com.mojtaba.folentra.macrobenchmark

import android.content.Intent
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke coverage for the installed, minified benchmark APK.
 *
 * This deliberately uses UI Automator instead of Compose test internals so the test exercises the
 * same process, navigation, Room database and release-only wiring as an installed candidate.
 */
@RunWith(AndroidJUnit4::class)
class ReleaseCandidateSmokeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    @Before
    fun resetCandidateDataAndLaunch() {
        device.wakeUp()
        device.executeShellCommand("wm dismiss-keyguard")
        seedDemoData()
        device.executeShellCommand("am force-stop ${BenchmarkConfig.PackageName}")
        val launchIntent = instrumentation.targetContext.packageManager
            .getLaunchIntentForPackage(BenchmarkConfig.PackageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: error("No launcher activity for ${BenchmarkConfig.PackageName}")
        instrumentation.targetContext.startActivity(launchIntent)
        device.waitForIdle()
        // Dashboard content is month-sensitive; the fixed demo dataset may not be in the device's
        // current month. Verify the stable destination title here and seeded records below.
        assertHasText("Dashboard")
    }

    @Test
    fun criticalReleaseCandidateFlows() {
        // Release-only safety: the destination must not exist in the installed benchmark UI.
        assertNull(device.findObject(By.text("Debug Health")))

        openDestination("Transactions")
        assertHasText("Neighborhood Market")
        createTransactionAndVerifyValidation()
        editCreatedTransaction()

        openDestination("Search")
        setText("Search transactions by keyword", CREATED_MERCHANT)
        assertHasText(CREATED_MERCHANT)

        openDestination("Transactions")
        deleteCreatedTransaction()

        openDestination("Settings")
        assertHasText("Security and privacy")
        assertHasDescription("App lock")
        assertHasDescription("Backup-ready profile")
        assertNull(device.findObject(By.text("Debug Health")))
    }

    private fun createTransactionAndVerifyValidation() {
        clickText("Add transaction")
        assertHasText("Create Transaction")

        assertFalse(
            "An incomplete transaction must not be saveable",
            descriptionIsEnabled("Save transaction"),
        )

        setText("Transaction amount", "42.75")
        clickDescription("Category Dining")
        setText("Merchant", CREATED_MERCHANT)

        assertTrue(
            "A valid transaction must be saveable",
            descriptionIsEnabled("Save transaction"),
        )
        clickDescription("Save transaction")
        assertHasText(CREATED_MERCHANT)
    }

    private fun editCreatedTransaction() {
        openTransactionDetail(CREATED_MERCHANT)
        clickDescription("Edit transaction")
        assertHasText("Edit Transaction")
        setText("Merchant", EDITED_MERCHANT)
        clickDescription("Save transaction")
        assertHasText(EDITED_MERCHANT)
    }

    private fun deleteCreatedTransaction() {
        openTransactionDetail(EDITED_MERCHANT)
        clickDescription("Delete transaction")
        assertHasText("Delete transaction?")
        clickDescription("Confirm delete transaction")
        assertTrue(
            "Deleted transaction remained visible",
            device.wait(Until.gone(By.text(EDITED_MERCHANT)), TIMEOUT_MILLIS),
        )
    }

    private fun seedDemoData() {
        device.executeShellCommand("logcat -c")
        val intent = Intent()
            .setClassName(BenchmarkConfig.PackageName, BenchmarkConfig.SetupActivity)
            .putExtra(BenchmarkConfig.SeedModeExtra, BenchmarkConfig.SeedModeDemo)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        instrumentation.targetContext.startActivity(intent)
        waitForBenchmarkSeedCompletion()
        device.waitForIdle()
    }

    private fun waitForBenchmarkSeedCompletion() {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MILLIS
        var setupLog = ""
        while (SystemClock.uptimeMillis() < deadline) {
            setupLog = device.executeShellCommand("logcat -d -s BenchmarkSetup:I *:S")
            check("Failed to seed benchmark data" !in setupLog) {
                "Benchmark data setup failed. $setupLog"
            }
            // Do not stop the app process until Room has closed and its WAL has been flushed.
            if ("Benchmark data setup complete" in setupLog) return
            SystemClock.sleep(SETUP_POLL_MILLIS)
        }
        error("Benchmark data setup did not complete. BenchmarkSetup log: $setupLog")
    }

    private fun openDestination(label: String) {
        val description = "$label navigation destination"
        if (!device.hasObject(By.desc(description)) &&
            device.hasObject(By.clazz("android.widget.EditText").focused(true))
        ) {
            device.pressBack()
            device.waitForIdle()
        }
        clickDescription(description)
        assertHasText(label)
    }

    private fun clickText(text: String) {
        retryStaleObject("click text '$text'") {
            val node = device.wait(Until.findObject(By.text(text)), TIMEOUT_MILLIS)
                ?: error("Timed out waiting for text: $text")
            findClickableAncestor(node, "text '$text'").click()
        }
        device.waitForIdle()
    }

    private fun openTransactionDetail(merchant: String) {
        repeat(TRANSACTION_OPEN_RETRIES) {
            val row = device.wait(
                Until.findObject(By.descContains(merchant)),
                TRANSACTION_OPEN_TIMEOUT_MILLIS,
            ) ?: run {
                val title = device.wait(
                    Until.findObject(By.text(merchant)),
                    TRANSACTION_OPEN_TIMEOUT_MILLIS,
                ) ?: error("Timed out waiting for transaction row: $merchant")
                findClickableAncestor(title, "transaction '$merchant'")
            }
            val bounds = row.visibleBounds
            device.click(bounds.centerX(), bounds.centerY())
            device.waitForIdle()
            if (device.wait(
                    Until.hasObject(By.desc("Edit transaction")),
                    TRANSACTION_OPEN_TIMEOUT_MILLIS,
                )
            ) {
                return
            }
        }
        error("Transaction details did not open for: $merchant")
    }

    private fun setText(description: String, value: String) {
        retryStaleObject("set text for '$description'") {
            findEditableField(description).click()
            SystemClock.sleep(INPUT_SETTLE_MILLIS)
            findEditableField(description).text = value
        }
        SystemClock.sleep(INPUT_SETTLE_MILLIS)
        device.waitForIdle()
    }

    private fun findEditableField(description: String): UiObject2 {
        val semanticsNode = findDescriptionWithScroll(description)
        return generateSequence(semanticsNode) { node -> node.parent }
            .firstOrNull { node ->
                node.className == "android.widget.EditText" ||
                    (node.isFocusable && node.isClickable)
            }
            ?: error("No editable field contains content description: $description")
    }

    private fun assertHasText(text: String) {
        if (!device.wait(Until.hasObject(By.text(text)), TIMEOUT_MILLIS)) {
            error("Timed out waiting for text: $text. ${visibleUiSummary()}")
        }
    }

    private fun visibleUiSummary(): String {
        val nodes = device.findObjects(By.depth(0))
        val labels = nodes.asSequence()
            .flatMap { it.flatten() }
            .mapNotNull { node ->
                try {
                    val label = node.text?.takeIf(String::isNotBlank)
                        ?: node.contentDescription?.takeIf(String::isNotBlank)
                    label?.let { "${node.applicationPackage}: $it" }
                } catch (_: StaleObjectException) {
                    null
                }
            }
            .distinct()
            .take(MAX_DIAGNOSTIC_LABELS)
            .toList()
        val packages = nodes.asSequence()
            .flatMap { it.flatten() }
            .mapNotNull { node ->
                try {
                    node.applicationPackage
                } catch (_: StaleObjectException) {
                    null
                }
            }
            .distinct()
            .toList()
        return "Visible packages: $packages; labels: $labels"
    }

    private fun UiObject2.flatten(): Sequence<UiObject2> = sequence {
        yield(this@flatten)
        try {
            children.forEach { child -> yieldAll(child.flatten()) }
        } catch (_: StaleObjectException) {
            // Compose may replace a semantics node while diagnostics are collected.
        }
    }

    private fun assertHasDescription(description: String) {
        findDescriptionWithScroll(description)
    }

    private fun clickDescription(description: String) {
        retryStaleObject("click description '$description'") {
            val node = findDescriptionWithScroll(description)
            findClickableAncestor(node, "content description '$description'").click()
        }
        device.waitForIdle()
    }

    private fun findClickableAncestor(node: UiObject2, label: String): UiObject2 =
        generateSequence(node) { current -> current.parent }
            .firstOrNull(UiObject2::isClickable)
            ?: error("No clickable UI node contains $label")

    private fun descriptionIsEnabled(description: String): Boolean =
        retryStaleObject("read enabled state for '$description'") {
            val semanticsNode = findDescriptionWithScroll(description)
            generateSequence(semanticsNode) { node -> node.parent }
                .firstOrNull { node -> node.isClickable || !node.isEnabled }
                ?.isEnabled
                ?: semanticsNode.isEnabled
        }

    private inline fun <T> retryStaleObject(operation: String, block: () -> T): T {
        var lastFailure: StaleObjectException? = null
        repeat(STALE_OBJECT_RETRIES) {
            try {
                return block()
            } catch (failure: StaleObjectException) {
                lastFailure = failure
                SystemClock.sleep(STALE_OBJECT_RETRY_MILLIS)
            }
        }
        throw AssertionError("Could not $operation after UI recomposition", lastFailure)
    }

    private fun findDescriptionWithScroll(description: String): UiObject2 {
        repeat(MAX_SCROLLS + 1) { attempt ->
            device.findObject(By.desc(description))?.let { return it }
            if (attempt < MAX_SCROLLS) {
                swipeContentUp()
                device.waitForIdle()
            }
        }

        // The previous lookup may have left a form at its bottom. Search back toward the top too.
        repeat(MAX_SCROLLS * 2) {
            swipeContentDown()
            device.waitForIdle()
            device.findObject(By.desc(description))?.let { return it }
        }
        error("Timed out waiting for content description: $description")
    }

    private fun swipeContentUp() {
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            device.displayWidth / 2,
            device.displayHeight / 4,
            SWIPE_STEPS,
        )
    }

    private fun swipeContentDown() {
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight / 4,
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            SWIPE_STEPS,
        )
    }

    private companion object {
        const val CREATED_MERCHANT = "RC Smoke Merchant"
        const val EDITED_MERCHANT = "RC Smoke Merchant Edited"
        const val TIMEOUT_MILLIS = 30_000L
        const val INPUT_SETTLE_MILLIS = 400L
        const val SETUP_POLL_MILLIS = 200L
        const val MAX_SCROLLS = 5
        const val SWIPE_STEPS = 12
        const val MAX_DIAGNOSTIC_LABELS = 40
        const val STALE_OBJECT_RETRIES = 4
        const val STALE_OBJECT_RETRY_MILLIS = 250L
        const val TRANSACTION_OPEN_RETRIES = 3
        const val TRANSACTION_OPEN_TIMEOUT_MILLIS = 3_000L
    }
}
