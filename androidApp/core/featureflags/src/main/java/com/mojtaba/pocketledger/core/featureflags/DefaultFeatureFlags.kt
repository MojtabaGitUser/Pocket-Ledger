package com.mojtaba.pocketledger.core.featureflags

object DefaultFeatureFlags {
    val SemanticSearchEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("semantic_search_enabled"),
        defaultValue = false,
        description = "Enables future AI or semantic transaction search surfaces.",
    )

    val AiInsightsEnabled = BooleanFeatureFlag(
        key = FeatureFlagKey("ai_insights_enabled"),
        defaultValue = false,
        description = "Enables future AI-generated dashboard insights.",
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
        PasskeyAccountFlowEnabled,
        CloudSyncEnabled,
        BackgroundJobsEnabled,
        DemoDataToolsEnabled,
        ScreenshotTestingEnabled,
    )
}
