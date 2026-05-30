package com.mojtaba.pocketledger.navigation

sealed interface AppDestination {
    val route: String
    val deepLinkPath: String
        get() = route

    data object Dashboard : AppDestination {
        override val route = "dashboard"
    }

    data object Transactions : AppDestination {
        override val route = "transactions"
    }

    data object Search : AppDestination {
        override val route = "search"
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
