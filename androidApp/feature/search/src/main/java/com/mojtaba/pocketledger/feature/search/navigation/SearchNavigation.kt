package com.mojtaba.pocketledger.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.ai.AiProviderSelector
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.feature.search.presentation.SearchRoute

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
