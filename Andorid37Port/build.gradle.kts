buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // AGP 9 ships with a runtime dependency on an older Kotlin Gradle Plugin; this pins the
        // Kotlin compiler used by built-in Kotlin to the version in the catalog.
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
