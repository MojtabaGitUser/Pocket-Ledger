package com.mojtaba.pocketledger.core.featureflags

object DefaultFeatureFlags {
    val SemanticSearchEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("semantic_search_enabled"),
        defaultValue = true,
        description = "Enables local semantic transaction search with deterministic fallback.",
    )

    val AiInsightsEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("ai_insights_enabled"),
        defaultValue = true,
        description = "Enables private on-device or rule-based monthly insights.",
    )

    val SmartAutofillEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("smart_autofill_enabled"),
        defaultValue = true,
        description = "Enables local smart transaction autofill suggestions.",
    )

    val PasskeyAccountFlowEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("passkey_account_flow_enabled"),
        defaultValue = false,
        description = "Enables future passkey-enabled account flow entry points.",
    )

    val CloudSyncEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("cloud_sync_enabled"),
        defaultValue = false,
        description = "Enables future cloud synchronization behavior.",
    )

    val BackgroundJobsEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("background_jobs_enabled"),
        defaultValue = false,
        description = "Enables future production background job scheduling.",
    )

    val DemoDataToolsEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("demo_data_tools_enabled"),
        defaultValue = false,
        description = "Enables internal demo data tooling entry points.",
    )

    val ScreenshotTestingEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("screenshot_testing_enabled"),
        defaultValue = false,
        description = "Enables future screenshot testing infrastructure entry points.",
    )

    val All: List<FeatureFlag<*>> = listOf(
        SemanticSearchEnabled,
        AiInsightsEnabled,
        SmartAutofillEnabled,
        PasskeyAccountFlowEnabled,
        CloudSyncEnabled,
        BackgroundJobsEnabled,
        DemoDataToolsEnabled,
        ScreenshotTestingEnabled,
    )
}
