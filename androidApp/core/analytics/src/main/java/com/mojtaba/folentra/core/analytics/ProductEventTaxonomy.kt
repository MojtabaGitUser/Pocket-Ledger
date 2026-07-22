package com.mojtaba.folentra.core.analytics

sealed interface ProductEvent {
    val name: ProductEventName
    val parameters: Set<ProductEventParameter>

    data class AppOpened(
        val buildType: BuildType,
        val appVersion: AppVersion? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.AppOpened
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.buildType(buildType))
            appVersion?.let { add(ProductEventParameter.appVersion(it)) }
        }
    }

    data object AppForegrounded : ProductEvent {
        override val name: ProductEventName = ProductEventName.AppForegrounded
        override val parameters: Set<ProductEventParameter> = emptySet()
    }

    data class ScreenViewed(
        val screenName: ProductScreen,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.ScreenViewed
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.screenName(screenName))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class TransactionCreated(
        val amountBucket: AmountBucket,
        val currencyPresent: Boolean,
        val isRecurring: Boolean,
        val hasAttachment: Boolean = false,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.TransactionCreated
        override val parameters: Set<ProductEventParameter> = transactionParameters(
            amountBucket = amountBucket,
            currencyPresent = currencyPresent,
            isRecurring = isRecurring,
            hasAttachment = hasAttachment,
            source = source,
        )
    }

    data class TransactionUpdated(
        val amountBucket: AmountBucket,
        val currencyPresent: Boolean,
        val isRecurring: Boolean,
        val hasAttachment: Boolean = false,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.TransactionUpdated
        override val parameters: Set<ProductEventParameter> = transactionParameters(
            amountBucket = amountBucket,
            currencyPresent = currencyPresent,
            isRecurring = isRecurring,
            hasAttachment = hasAttachment,
            source = source,
        )
    }

    data class TransactionDeleted(
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.TransactionDeleted
        override val parameters: Set<ProductEventParameter> = optionalSource(source)
    }

    data class TransactionSaveFailed(
        val errorType: ErrorType,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.TransactionSaveFailed
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.errorType(errorType))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class DashboardSummaryViewed(
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.DashboardSummaryViewed
        override val parameters: Set<ProductEventParameter> = optionalSource(source)
    }

    data class DashboardPeriodChanged(
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.DashboardPeriodChanged
        override val parameters: Set<ProductEventParameter> = optionalSource(source)
    }

    data class SearchPerformed(
        val result: EventResult,
        val countBucket: CountBucket,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.SearchPerformed
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.result(result))
            add(ProductEventParameter.countBucket(countBucket))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class FilterApplied(
        val itemType: ItemType,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.FilterApplied
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.itemType(itemType))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class BudgetCreated(
        val amountBucket: AmountBucket,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.BudgetCreated
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.amountBucket(amountBucket))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class BudgetUpdated(
        val amountBucket: AmountBucket,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.BudgetUpdated
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.amountBucket(amountBucket))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class CategorySelected(
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.CategorySelected
        override val parameters: Set<ProductEventParameter> = optionalSource(source)
    }

    data class SyncStarted(
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.SyncStarted
        override val parameters: Set<ProductEventParameter> = optionalSource(source)
    }

    data class SyncCompleted(
        val result: EventResult,
        val itemType: ItemType? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.SyncCompleted
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.result(result))
            itemType?.let { add(ProductEventParameter.itemType(it)) }
        }
    }

    data class SyncFailed(
        val errorType: ErrorType,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.SyncFailed
        override val parameters: Set<ProductEventParameter> = setOf(ProductEventParameter.errorType(errorType))
    }

    data class AiFeatureUsed(
        val itemType: ItemType,
        val result: EventResult,
        val featureFlagState: FeatureFlagState,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.AiFeatureUsed
        override val parameters: Set<ProductEventParameter> = setOf(
            ProductEventParameter.itemType(itemType),
            ProductEventParameter.result(result),
            ProductEventParameter.featureFlagState(featureFlagState),
        )
    }

    data class SecuritySettingChanged(
        val itemType: ItemType,
        val result: EventResult,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.SecuritySettingChanged
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.itemType(itemType))
            add(ProductEventParameter.result(result))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class AppLockAuthenticationCompleted(
        val result: EventResult,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.AppLockAuthenticationCompleted
        override val parameters: Set<ProductEventParameter> = setOf(ProductEventParameter.result(result))
    }

    data class ErrorRecovered(
        val errorType: ErrorType,
        val source: EventSource? = null,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.ErrorRecovered
        override val parameters: Set<ProductEventParameter> = buildSet {
            add(ProductEventParameter.errorType(errorType))
            source?.let { add(ProductEventParameter.source(it)) }
        }
    }

    data class DebugHealthOpened(
        val buildType: BuildType,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.DebugHealthOpened
        override val parameters: Set<ProductEventParameter> = setOf(ProductEventParameter.buildType(buildType))
    }

    data class InternalDistributionReadinessViewed(
        val result: EventResult,
    ) : ProductEvent {
        override val name: ProductEventName = ProductEventName.InternalDistributionReadinessViewed
        override val parameters: Set<ProductEventParameter> = setOf(ProductEventParameter.result(result))
    }
}

private fun transactionParameters(
    amountBucket: AmountBucket,
    currencyPresent: Boolean,
    isRecurring: Boolean,
    hasAttachment: Boolean,
    source: EventSource?,
): Set<ProductEventParameter> = buildSet {
    add(ProductEventParameter.amountBucket(amountBucket))
    add(ProductEventParameter.currencyPresent(currencyPresent))
    add(ProductEventParameter.isRecurring(isRecurring))
    add(ProductEventParameter.hasAttachment(hasAttachment))
    source?.let { add(ProductEventParameter.source(it)) }
}

private fun optionalSource(source: EventSource?): Set<ProductEventParameter> =
    source?.let { setOf(ProductEventParameter.source(it)) } ?: emptySet()

enum class ProductEventName(val value: String) {
    AppOpened("app_opened"),
    AppForegrounded("app_foregrounded"),
    ScreenViewed("screen_viewed"),
    TransactionCreated("transaction_created"),
    TransactionUpdated("transaction_updated"),
    TransactionDeleted("transaction_deleted"),
    TransactionSaveFailed("transaction_save_failed"),
    DashboardSummaryViewed("dashboard_summary_viewed"),
    DashboardPeriodChanged("dashboard_period_changed"),
    SearchPerformed("search_performed"),
    FilterApplied("filter_applied"),
    BudgetCreated("budget_created"),
    BudgetUpdated("budget_updated"),
    CategorySelected("category_selected"),
    SyncStarted("sync_started"),
    SyncCompleted("sync_completed"),
    SyncFailed("sync_failed"),
    AiFeatureUsed("ai_feature_used"),
    SecuritySettingChanged("security_setting_changed"),
    AppLockAuthenticationCompleted("app_lock_authentication_completed"),
    ErrorRecovered("error_recovered"),
    DebugHealthOpened("debug_health_opened"),
    InternalDistributionReadinessViewed("internal_distribution_readiness_viewed"),
}

enum class ProductEventParameterKey(val value: String) {
    ScreenName("screen_name"),
    Source("source"),
    Result("result"),
    ErrorType("error_type"),
    ItemType("item_type"),
    CountBucket("count_bucket"),
    AmountBucket("amount_bucket"),
    CurrencyPresent("currency_present"),
    IsRecurring("is_recurring"),
    HasAttachment("has_attachment"),
    BuildType("build_type"),
    AppVersion("app_version"),
    FeatureFlagState("feature_flag_state"),
}

class ProductEventParameter private constructor(
    val key: ProductEventParameterKey,
    val value: String,
) {
    override fun equals(other: Any?): Boolean =
        other is ProductEventParameter && key == other.key && value == other.value

    override fun hashCode(): Int = 31 * key.hashCode() + value.hashCode()

    override fun toString(): String = "ProductEventParameter(key=$key, value=$value)"

    companion object {
        fun screenName(value: ProductScreen): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.ScreenName, value.value)

        fun source(value: EventSource): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.Source, value.value)

        fun result(value: EventResult): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.Result, value.value)

        fun errorType(value: ErrorType): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.ErrorType, value.value)

        fun itemType(value: ItemType): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.ItemType, value.value)

        fun countBucket(value: CountBucket): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.CountBucket, value.value)

        fun amountBucket(value: AmountBucket): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.AmountBucket, value.value)

        fun currencyPresent(value: Boolean): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.CurrencyPresent, value.toAnalyticsValue())

        fun isRecurring(value: Boolean): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.IsRecurring, value.toAnalyticsValue())

        fun hasAttachment(value: Boolean): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.HasAttachment, value.toAnalyticsValue())

        fun buildType(value: BuildType): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.BuildType, value.value)

        fun appVersion(value: AppVersion): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.AppVersion, value.value)

        fun featureFlagState(value: FeatureFlagState): ProductEventParameter =
            ProductEventParameter(ProductEventParameterKey.FeatureFlagState, value.value)
    }
}

@JvmInline
value class AppVersion(val value: String) {
    init {
        require(value.matches(APP_VERSION_REGEX)) {
            "App version must be provider-safe and must not contain user data."
        }
    }

    private companion object {
        val APP_VERSION_REGEX = Regex("^[A-Za-z0-9._+-]{1,32}$")
    }
}

enum class ProductScreen(val value: String) {
    AppLock("app_lock"),
    BudgetSetup("budget_setup"),
    Dashboard("dashboard"),
    DebugHealth("debug_health"),
    Insights("insights"),
    Search("search"),
    Settings("settings"),
    TransactionDetail("transaction_detail"),
    TransactionEditor("transaction_editor"),
    TransactionList("transaction_list"),
}

enum class EventSource(val value: String) {
    AppStartup("app_startup"),
    Dashboard("dashboard"),
    DeepLink("deep_link"),
    DebugHealth("debug_health"),
    InternalDistribution("internal_distribution"),
    Navigation("navigation"),
    Search("search"),
    Settings("settings"),
    TransactionEditor("transaction_editor"),
    TransactionList("transaction_list"),
    WorkManager("work_manager"),
}

enum class EventResult(val value: String) {
    Success("success"),
    Failure("failure"),
    Cancelled("cancelled"),
    Empty("empty"),
    Unavailable("unavailable"),
}

enum class ErrorType(val value: String) {
    AuthenticationUnavailable("authentication_unavailable"),
    Database("database"),
    FeatureUnavailable("feature_unavailable"),
    NetworkUnavailable("network_unavailable"),
    PermissionDenied("permission_denied"),
    Validation("validation"),
    Unknown("unknown"),
}

enum class ItemType(val value: String) {
    Account("account"),
    AiInsight("ai_insight"),
    Budget("budget"),
    Category("category"),
    DashboardSummary("dashboard_summary"),
    Export("export"),
    Import("import"),
    SearchFilter("search_filter"),
    SecuritySetting("security_setting"),
    Sync("sync"),
    Tag("tag"),
    Transaction("transaction"),
}

enum class CountBucket(val value: String) {
    Zero("0"),
    One("1"),
    TwoToFive("2_5"),
    SixToTwenty("6_20"),
    TwentyOnePlus("21_plus"),
}

enum class AmountBucket(val value: String) {
    NotProvided("not_provided"),
    Low("low"),
    Medium("medium"),
    High("high"),
}

enum class BuildType(val value: String) {
    Debug("debug"),
    Release("release"),
    Benchmark("benchmark"),
    Unknown("unknown"),
}

enum class FeatureFlagState(val value: String) {
    Enabled("enabled"),
    Disabled("disabled"),
    Unavailable("unavailable"),
}

private fun Boolean.toAnalyticsValue(): String = if (this) "true" else "false"