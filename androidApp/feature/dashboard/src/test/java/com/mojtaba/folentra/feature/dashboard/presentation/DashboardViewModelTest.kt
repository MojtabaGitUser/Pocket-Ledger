package com.mojtaba.folentra.feature.dashboard.presentation

import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.ai.AiProviderSelector
import com.mojtaba.folentra.core.ai.NoOpAiProvider
import com.mojtaba.folentra.core.ai.RuleBasedAiProvider
import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.core.featureflags.FeatureFlagValue
import com.mojtaba.folentra.core.featureflags.LocalFeatureFlagProvider
import com.mojtaba.folentra.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.folentra.core.testing.fixture.TestClock
import com.mojtaba.folentra.core.testing.fixture.testIncomeTransaction
import com.mojtaba.folentra.core.testing.fixture.testLedgerBudget
import com.mojtaba.folentra.core.testing.fixture.testLedgerCategory
import com.mojtaba.folentra.core.testing.fixture.testLedgerTransaction
import com.mojtaba.folentra.core.testing.repository.FakeBudgetRepository
import com.mojtaba.folentra.core.testing.repository.FakeCategoryRepository
import com.mojtaba.folentra.core.testing.repository.FakeTransactionRepository
import com.mojtaba.folentra.feature.dashboard.domain.DashboardSummaryGenerator
import com.mojtaba.folentra.feature.dashboard.model.DashboardInsight
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun productionDashboardFlowUsesSummaryGeneratorWhenAiInsightsEnabled() = runTest {
        val viewModel = newViewModel(aiInsightsEnabled = true)
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DashboardUiState.Content)
        state as DashboardUiState.Content
        assertTrue(state.summary.insights.any { it is DashboardInsight.AiMonthlySummary })
        job.cancel()
    }

    @Test
    fun aiDisabledKeepsProductionDashboardRuleBasedOnly() = runTest {
        val viewModel = newViewModel(aiInsightsEnabled = false)
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DashboardUiState.Content)
        state as DashboardUiState.Content
        assertTrue(state.summary.insights.any { it is DashboardInsight.PositiveCashFlow })
        assertFalse(state.summary.insights.any { it is DashboardInsight.AiMonthlySummary })
        job.cancel()
    }

    @Test
    fun emptyLocalDataKeepsDashboardUsableWithoutAi() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(),
            budgetRepository = FakeBudgetRepository(),
            aiInsightsEnabled = true,
        )
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DashboardUiState.Empty)
        job.cancel()
    }

    private fun newViewModel(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(
                testIncomeTransaction().copy(occurredAt = TestClock.November16),
                testLedgerTransaction(occurredAt = TestClock.November14),
            ),
        ),
        budgetRepository: FakeBudgetRepository = FakeBudgetRepository(
            initialBudgets = listOf(testLedgerBudget(periodStart = TestClock.NovemberPeriodStart, periodEnd = TestClock.NovemberPeriodEnd)),
        ),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(
            initialCategories = listOf(testLedgerCategory()),
        ),
        aiInsightsEnabled: Boolean,
    ): DashboardViewModel {
        val featureFlags = FeatureFlagEvaluator(
            LocalFeatureFlagProvider(
                mapOf(DefaultFeatureFlags.AiInsightsEnabled.key to FeatureFlagValue.BooleanValue(aiInsightsEnabled)),
            ),
        )
        val selector = AiProviderSelector(
            providers = listOf(RuleBasedAiProvider, NoOpAiProvider),
            featureFlags = featureFlags,
        )
        return DashboardViewModel(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            summaryGenerator = DashboardSummaryGenerator(AiFallbackStrategy(selector)),
            currentTimeMillis = { TestClock.November16 },
            zoneId = ZoneOffset.UTC,
        )
    }
}
