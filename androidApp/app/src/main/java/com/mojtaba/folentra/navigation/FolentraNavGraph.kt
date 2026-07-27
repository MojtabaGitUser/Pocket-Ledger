package com.mojtaba.folentra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.mojtaba.folentra.account.OptionalAccountSettingsState
import com.mojtaba.folentra.adaptive.LocalAdaptiveNavigationState
import com.mojtaba.folentra.FolentraAppGraph
import com.mojtaba.folentra.debugflags.DebugFeatureFlagOverridesScreen
import com.mojtaba.folentra.debughealth.DebugHealthScreen
import com.mojtaba.folentra.feature.dashboard.insights.InsightsRoute
import com.mojtaba.folentra.feature.dashboard.navigation.dashboardGraph
import com.mojtaba.folentra.feature.search.navigation.searchGraph
import com.mojtaba.folentra.feature.transaction.navigation.TransactionRoutes
import com.mojtaba.folentra.feature.transaction.navigation.transactionGraph

@Composable
fun FolentraNavGraph(
    navController: NavHostController,
    startDestination: AppDestination,
    includeDebugDestinations: Boolean,
    appGraph: FolentraAppGraph,
    modifier: Modifier = Modifier,
) {
    val adaptiveNavigationState = LocalAdaptiveNavigationState.current

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
    ) {
        dashboardGraph(
            navController = navController,
            transactionRepository = appGraph.transactionRepository,
            budgetRepository = appGraph.budgetRepository,
            categoryRepository = appGraph.categoryRepository,
            aiFallbackStrategy = appGraph.aiFallbackStrategy,
            deepLinkBaseUri = "${AppDestination.DEEP_LINK_SCHEME}://${AppDestination.DEEP_LINK_HOST}",
            widthSizeClass = adaptiveNavigationState.widthSizeClass,
        )
        transactionGraph(
            navController = navController,
            transactionRepository = appGraph.transactionRepository,
            categoryRepository = appGraph.categoryRepository,
            tagRepository = appGraph.tagRepository,
            aiFallbackStrategy = appGraph.aiFallbackStrategy,
            deepLinkBaseUri = "${AppDestination.DEEP_LINK_SCHEME}://${AppDestination.DEEP_LINK_HOST}",
            paneType = adaptiveNavigationState.paneType,
        )
        searchGraph(
            transactionRepository = appGraph.transactionRepository,
            categoryRepository = appGraph.categoryRepository,
            tagRepository = appGraph.tagRepository,
            featureFlags = appGraph.featureFlags,
            aiProviderSelector = appGraph.aiProviderSelector,
            aiFallbackStrategy = appGraph.aiFallbackStrategy,
            deepLinkBaseUri = "${AppDestination.DEEP_LINK_SCHEME}://${AppDestination.DEEP_LINK_HOST}",
            onOpenTransaction = { transactionId ->
                navController.navigate(TransactionRoutes.detailRoute(transactionId))
            },
        )
        placeholderDestination(AppDestination.Insights) {
            InsightsRoute(
                transactionRepository = appGraph.transactionRepository,
                budgetRepository = appGraph.budgetRepository,
                categoryRepository = appGraph.categoryRepository,
                aiFallbackStrategy = appGraph.aiFallbackStrategy,
            )
        }
        placeholderDestination(AppDestination.Settings) {
            SettingsScreen(
                appLockManager = appGraph.appLockManager,
                backgroundJobSettingsManager = appGraph.backgroundJobSettingsManager,
                optionalAccountSettingsState = OptionalAccountSettingsState.from(
                    featureFlags = appGraph.featureFlags,
                    passkeyClient = appGraph.passkeyClient,
                    playIntegrityRequestHook = appGraph.playIntegrityRequestHook,
                ),
                backupReadyProfileManager = appGraph.backupReadyProfileManager,
            )
        }
        if (includeDebugDestinations) {
            placeholderDestination(AppDestination.DebugHealth) {
                DebugHealthScreen(appGraph = appGraph)
            }
            placeholderDestination(AppDestination.DebugFeatureFlags) {
                DebugFeatureFlagOverridesScreen(appGraph = appGraph)
            }
        }
    }
}

private fun NavGraphBuilder.placeholderDestination(
    destination: AppDestination,
    content: @Composable () -> Unit,
) {
    composable(
        route = destination.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = AppDestination.deepLinkUri(destination)
            },
        ),
    ) {
        content()
    }
}
