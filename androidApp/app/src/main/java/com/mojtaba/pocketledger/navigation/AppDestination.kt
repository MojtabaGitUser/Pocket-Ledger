package com.mojtaba.pocketledger.navigation

import com.mojtaba.pocketledger.feature.dashboard.navigation.DashboardRoutes
import com.mojtaba.pocketledger.feature.search.navigation.SearchRoutes
import com.mojtaba.pocketledger.feature.transaction.navigation.TransactionRoutes

sealed interface AppDestination {
    val route: String
    val deepLinkPath: String
        get() = route

    data object Dashboard : AppDestination {
        override val route = DashboardRoutes.DashboardRoute
    }

    data object Transactions : AppDestination {
        override val route = TransactionRoutes.ListRoute
    }

    data object Search : AppDestination {
        override val route = SearchRoutes.SearchRoute
    }

    data object Insights : AppDestination {
        override val route = "insights"
    }

    data object Settings : AppDestination {
        override val route = "settings"
    }

    data object DebugHealth : AppDestination {
        override val route = "debug/health"
    }

    companion object {
        const val DEEP_LINK_SCHEME = "pocketledger"
        const val DEEP_LINK_HOST = "app"

        fun deepLinkUri(destination: AppDestination): String =
            "$DEEP_LINK_SCHEME://$DEEP_LINK_HOST/${destination.deepLinkPath}"
    }
}
