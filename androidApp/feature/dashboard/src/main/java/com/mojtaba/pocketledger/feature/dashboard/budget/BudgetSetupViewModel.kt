package com.mojtaba.pocketledger.feature.dashboard.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BudgetSetupViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
    initialMode: BudgetSetupMode = BudgetSetupMode.CREATE,
    initialBudgetId: String? = null,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) : ViewModel() {
    private val defaultPeriod = monthPeriodFor(currentTimeMillis(), zoneId)
    private val _uiState = MutableStateFlow(
        BudgetSetupUiState(
            formState = restoredFormState(initialMode, initialBudgetId),
            isLoading = shouldLoadEditBudget(initialMode),
        ),
    )
    val uiState: StateFlow<BudgetSetupUiState> = _uiState.asStateFlow()

    private val _events = Channel<BudgetSetupEvent>(Channel.BUFFERED)
    val events: Flow<BudgetSetupEvent> = _events.receiveAsFlow()

    init {
        updateValidation()
        observeCategories()
        loadEditBudgetIfNeeded(initialMode)
    }

    fun onAction(action: BudgetSetupAction) {
        when (action) {
            is BudgetSetupAction.NameChanged -> updateForm { copy(nameInput = action.value) }
            is BudgetSetupAction.AmountChanged -> updateForm { copy(amountInput = action.value) }
            is BudgetSetupAction.CurrencyChanged -> updateForm { copy(currencyCode = action.value) }
            is BudgetSetupAction.CategorySelected -> updateForm { copy(categoryId = action.categoryId) }
            is BudgetSetupAction.PeriodChanged -> updateForm {
                copy(periodStart = action.periodStart, periodEnd = action.periodEnd)
            }
            is BudgetSetupAction.ActiveChanged -> updateForm { copy(isActive = action.value) }
            BudgetSetupAction.SaveClicked -> save()
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.observeActiveCategories().collect { categories ->
                _uiState.update { state ->
                    state.copy(
                        categories = categories.map {
                            BudgetCategoryOption(
                                id = it.id,
                                name = it.name,
                                type = it.type,
                            )
                        },
                    )
                }
            }
        }
    }

    private fun loadEditBudgetIfNeeded(initialMode: BudgetSetupMode) {
        if (!shouldLoadEditBudget(initialMode)) return

        viewModelScope.launch {
            val budgetId = _uiState.value.formState.budgetId
            if (budgetId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(BudgetSetupEvent.ShowSnackbar("Budget id is required for editing."))
                return@launch
            }

            val budget = budgetRepository.getById(budgetId)
            if (budget == null) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(BudgetSetupEvent.ShowSnackbar("Budget was not found."))
                return@launch
            }

            val formState = budget.toFormState()
            persistFormState(formState)
            savedStateHandle[KEY_EDIT_LOADED] = true
            _uiState.update {
                it.copy(
                    formState = formState,
                    isLoading = false,
                )
            }
            updateValidation()
        }
    }

    private fun save() {
        val validation = BudgetSetupValidation.validate(_uiState.value.formState)
        if (!validation.isValid) {
            _uiState.update { it.copy(validationResult = validation) }
            viewModelScope.launch {
                _events.send(BudgetSetupEvent.ShowSnackbar("Fix validation errors before saving."))
            }
            return
        }

        val input = requireNotNull(validation.validatedInput)
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationResult = validation) }
            runCatching {
                saveValidatedInput(input, _uiState.value.formState.mode)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.send(BudgetSetupEvent.SaveCompleted)
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSaving = false) }
                _events.send(
                    BudgetSetupEvent.ShowSnackbar(
                        throwable.message ?: "Unable to save budget.",
                    ),
                )
            }
        }
    }

    private suspend fun saveValidatedInput(
        input: ValidatedBudgetInput,
        mode: BudgetSetupMode,
    ) {
        val now = currentTimeMillis()
        val budgetId = input.id ?: idGenerator()
        val existing = if (mode == BudgetSetupMode.EDIT) {
            budgetRepository.getById(budgetId)
        } else {
            null
        }

        budgetRepository.upsert(
            LedgerBudget(
                id = budgetId,
                name = input.name,
                amountMinor = input.amountMinor,
                currencyCode = input.currencyCode,
                periodType = input.periodType,
                periodStart = input.periodStart,
                periodEnd = input.periodEnd,
                categoryId = input.categoryId,
                isActive = input.isActive,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    private fun updateForm(reducer: BudgetSetupState.() -> BudgetSetupState) {
        val updatedFormState = _uiState.value.formState.reducer()
        persistFormState(updatedFormState)
        _uiState.update { it.copy(formState = updatedFormState) }
        updateValidation()
    }

    private fun updateValidation() {
        _uiState.update {
            it.copy(validationResult = BudgetSetupValidation.validate(it.formState))
        }
    }

    private fun shouldLoadEditBudget(initialMode: BudgetSetupMode): Boolean =
        restoredMode(initialMode) == BudgetSetupMode.EDIT &&
            savedStateHandle.get<Boolean>(KEY_EDIT_LOADED) != true &&
            savedStateHandle.get<Boolean>(KEY_FORM_RESTORED) != true

    private fun restoredFormState(
        initialMode: BudgetSetupMode,
        initialBudgetId: String?,
    ): BudgetSetupState {
        val mode = restoredMode(initialMode)
        return BudgetSetupState(
            mode = mode,
            budgetId = savedStateHandle[KEY_BUDGET_ID] ?: initialBudgetId,
            nameInput = savedStateHandle[KEY_NAME_INPUT] ?: "",
            amountInput = savedStateHandle[KEY_AMOUNT_INPUT] ?: "",
            currencyCode = savedStateHandle[KEY_CURRENCY_CODE] ?: BudgetSetupState.DEFAULT_CURRENCY_CODE,
            categoryId = savedStateHandle[KEY_CATEGORY_ID],
            periodStart = savedStateHandle[KEY_PERIOD_START] ?: defaultPeriod.startMillis,
            periodEnd = savedStateHandle[KEY_PERIOD_END] ?: defaultPeriod.endMillis,
            isActive = savedStateHandle[KEY_IS_ACTIVE] ?: true,
        )
    }

    private fun restoredMode(initialMode: BudgetSetupMode): BudgetSetupMode =
        savedStateHandle.get<String>(KEY_MODE)
            ?.let { runCatching { BudgetSetupMode.valueOf(it) }.getOrNull() }
            ?: initialMode

    private fun persistFormState(state: BudgetSetupState) {
        savedStateHandle[KEY_FORM_RESTORED] = true
        savedStateHandle[KEY_MODE] = state.mode.name
        savedStateHandle[KEY_BUDGET_ID] = state.budgetId
        savedStateHandle[KEY_NAME_INPUT] = state.nameInput
        savedStateHandle[KEY_AMOUNT_INPUT] = state.amountInput
        savedStateHandle[KEY_CURRENCY_CODE] = state.currencyCode
        savedStateHandle[KEY_CATEGORY_ID] = state.categoryId
        savedStateHandle[KEY_PERIOD_START] = state.periodStart
        savedStateHandle[KEY_PERIOD_END] = state.periodEnd
        savedStateHandle[KEY_IS_ACTIVE] = state.isActive
    }

    private fun LedgerBudget.toFormState(): BudgetSetupState = BudgetSetupState(
        mode = BudgetSetupMode.EDIT,
        budgetId = id,
        nameInput = name,
        amountInput = amountMinor.toDisplayAmountInput(),
        currencyCode = currencyCode,
        categoryId = categoryId,
        periodStart = periodStart,
        periodEnd = periodEnd,
        isActive = isActive,
    )

    private fun Long.toDisplayAmountInput(): String =
        BigDecimal.valueOf(this)
            .movePointLeft(2)
            .setScale(2, RoundingMode.UNNECESSARY)
            .stripTrailingZeros()
            .toPlainString()

    private data class MonthPeriod(
        val startMillis: Long,
        val endMillis: Long,
    )

    private companion object {
        const val KEY_FORM_RESTORED = "budget_setup_form_restored"
        const val KEY_EDIT_LOADED = "budget_setup_edit_loaded"
        const val KEY_MODE = "budget_setup_mode"
        const val KEY_BUDGET_ID = "budget_setup_budget_id"
        const val KEY_NAME_INPUT = "budget_setup_name_input"
        const val KEY_AMOUNT_INPUT = "budget_setup_amount_input"
        const val KEY_CURRENCY_CODE = "budget_setup_currency_code"
        const val KEY_CATEGORY_ID = "budget_setup_category_id"
        const val KEY_PERIOD_START = "budget_setup_period_start"
        const val KEY_PERIOD_END = "budget_setup_period_end"
        const val KEY_IS_ACTIVE = "budget_setup_is_active"

        fun monthPeriodFor(
            timeMillis: Long,
            zoneId: ZoneId,
        ): MonthPeriod {
            val monthStart = Instant.ofEpochMilli(timeMillis)
                .atZone(zoneId)
                .toLocalDate()
                .withDayOfMonth(1)
            return MonthPeriod(
                startMillis = monthStart.toStartOfDayMillis(zoneId),
                endMillis = monthStart.plusMonths(1).toStartOfDayMillis(zoneId) - 1L,
            )
        }

        private fun LocalDate.toStartOfDayMillis(zoneId: ZoneId): Long =
            atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
}
