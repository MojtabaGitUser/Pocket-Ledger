pluginManagement {
    includeBuild("androidApp/build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("androidApp/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "PocketLedger"

include(":app")
include(":core:background")
include(":core:ai")
include(":core:analytics")
include(":core:featureflags")
include(":core:security")
include(":core:designsystem")
include(":core:data")
include(":core:database")
include(":core:testing")
include(":feature:dashboard")
include(":feature:search")
include(":feature:transaction")
include(":macrobenchmark")
include(":shared")

project(":core").projectDir = file("androidApp/core")
project(":app").projectDir = file("androidApp/app")
project(":core:background").projectDir = file("androidApp/core/background")
project(":core:ai").projectDir = file("androidApp/core/ai")
project(":core:analytics").projectDir = file("androidApp/core/analytics")
project(":core:featureflags").projectDir = file("androidApp/core/featureflags")
project(":core:security").projectDir = file("androidApp/core/security")
project(":core:designsystem").projectDir = file("androidApp/core/designsystem")
project(":core:data").projectDir = file("androidApp/core/data")
project(":core:database").projectDir = file("androidApp/core/database")
project(":core:testing").projectDir = file("androidApp/core/testing")
project(":feature").projectDir = file("androidApp/feature")
project(":feature:dashboard").projectDir = file("androidApp/feature/dashboard")
project(":feature:search").projectDir = file("androidApp/feature/search")
project(":feature:transaction").projectDir = file("androidApp/feature/transaction")
project(":macrobenchmark").projectDir = file("androidApp/macrobenchmark")
project(":shared").projectDir = file("androidApp/shared")
