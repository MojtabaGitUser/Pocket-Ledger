# Folentra intentionally keeps app-specific R8 rules narrow.
#
# Current release builds rely on:
# - Android's optimized default rules from proguard-android-optimize.txt.
# - Consumer rules shipped by AndroidX Room, WorkManager, Security Crypto,
#   ProfileInstaller, Compose/Lifecycle, and Tink.
# - compileOnly annotation artifacts in :app and :core:security so R8 can
#   resolve Tink's Error Prone and JSR-305 annotation references without
#   packaging annotation-only jars as runtime dependencies.
#
# Do not add broad keep/dontwarn rules here. If R8 emits missing_rules.txt,
# classify each missing type as annotation metadata, optional integration, or a
# real runtime dependency before adding a targeted rule or dependency.
