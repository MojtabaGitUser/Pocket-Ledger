package com.mojtaba.pocketledger.navigation

enum class TopLevelDestination(
    val destination: AppDestination,
    val label: String,
    val shortLabel: String,
) {
    Dashboard(
        destination = AppDestination.Dashboard,
        label = "Dashboard",
        shortLabel = "D",
    ),
    Transactions(
        destination = AppDestination.Transactions,
        label = "Transactions",
        shortLabel = "T",
    ),
    Search(
        destination = AppDestination.Search,
        label = "Search",
        shortLabel = "S",
    ),
    Insights(
        destination = AppDestination.Insights,
        label = "Insights",
        shortLabel = "I",
    ),
    Settings(
        destination = AppDestination.Settings,
        label = "Settings",
        shortLabel = "S",
    ),
    DebugHealth(
        destination = AppDestination.DebugHealth,
        label = "Debug",
        shortLabel = "D",
    ),
}
