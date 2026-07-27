package com.mojtaba.folentra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val appGraph: FolentraAppGraph by lazy {
        FolentraAppGraph.create(
            context = applicationContext,
            activityProvider = { this },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appGraph.startupFailureReporter.markStartupStarted("main_activity_on_create")
        try {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                FolentraTheme {
                    FolentraApp(appGraph = appGraph)
                }
            }
            appGraph.startupFailureReporter.markStartupCompleted()
        } catch (throwable: Throwable) {
            appGraph.startupFailureReporter.recordCriticalFailure(
                throwable = throwable,
                stage = "main_activity_on_create",
            )
            throw throwable
        }
    }

    override fun onStart() {
        super.onStart()
        activityScope.launch {
            appGraph.appLockManager.onAppForegrounded()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}
