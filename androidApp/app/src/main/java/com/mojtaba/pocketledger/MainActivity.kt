package com.mojtaba.pocketledger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val appGraph: PocketLedgerAppGraph by lazy {
        PocketLedgerAppGraph.create(
            context = applicationContext,
            activityProvider = { this },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketLedgerTheme {
                PocketLedgerApp(appGraph = appGraph)
            }
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
