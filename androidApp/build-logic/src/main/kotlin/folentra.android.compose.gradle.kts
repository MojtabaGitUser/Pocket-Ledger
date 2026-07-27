import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val composeReportsEnabled = providers.gradleProperty("folentra.composeReports")
    .map(String::toBoolean)
    .orElse(false)

extensions.configure<ComposeCompilerGradlePluginExtension>("composeCompiler") {
    if (composeReportsEnabled.get()) {
        reportsDestination.set(layout.buildDirectory.dir("compose_compiler"))
        metricsDestination.set(layout.buildDirectory.dir("compose_compiler"))
    }
}

pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension>("android") {
        buildFeatures {
            compose = true
        }
    }
}

pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension>("android") {
        buildFeatures {
            compose = true
        }
    }
}

dependencies {
    add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
    add("implementation", libs.findLibrary("androidx-compose-ui").get())
    add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
    add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
}
