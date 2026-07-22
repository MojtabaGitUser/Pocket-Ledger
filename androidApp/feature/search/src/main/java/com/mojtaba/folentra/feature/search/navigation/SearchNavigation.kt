package com.mojtaba.folentra.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.ai.AiProviderSelector
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.feature.search.presentation.SearchRoute

fun NavGraphBuilder.searchGraph(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    featureFlags: FeatureFlagEvaluator,
    aiProviderSelector: AiProviderSelector,
    aiFallbackStrategy: AiFallbackStrategy,
    deepLinkBaseUri: String,
    onOpenTransaction: (String) -> Unit,
) {
    composable(
        route = SearchRoutes.SearchRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = "$deepLinkBaseUri/${SearchRoutes.SearchRoute}" },
        ),
    ) {
        SearchRoute(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            featureFlags = featureFlags,
            aiProviderSelector = aiProviderSelector,
            aiFallbackStrategy = aiFallbackStrategy,
            onOpenTransaction = onOpenTransaction,
        )
    }
}
