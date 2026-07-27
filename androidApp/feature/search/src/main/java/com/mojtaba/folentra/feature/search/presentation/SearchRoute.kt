package com.mojtaba.folentra.feature.search.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.ai.AiProviderSelector
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator

@Composable
fun SearchRoute(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    featureFlags: FeatureFlagEvaluator,
    aiProviderSelector: AiProviderSelector,
    aiFallbackStrategy: AiFallbackStrategy,
    onOpenTransaction: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            featureFlags = featureFlags,
            aiProviderSelector = aiProviderSelector,
            aiFallbackStrategy = aiFallbackStrategy,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchEffect.OpenTransaction -> onOpenTransaction(effect.transactionId)
            }
        }
    }

    SearchScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

private class SearchViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val featureFlags: FeatureFlagEvaluator,
    private val aiProviderSelector: AiProviderSelector,
    private val aiFallbackStrategy: AiFallbackStrategy,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        if (!modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return SearchViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            featureFlags = featureFlags,
            aiProviderSelector = aiProviderSelector,
            aiFallbackStrategy = aiFallbackStrategy,
            savedStateHandle = extras.createSavedStateHandle(),
        ) as T
    }
}
