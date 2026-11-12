import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun demoValue(name: String): String =
  providers.gradleProperty(name).orElse(providers.environmentVariable(name)).orElse("").get()

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  namespace = "com.toters.lokalise.sample"
  compileSdk = 36
  defaultConfig {
    applicationId = "com.toters.lokalise.sample"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }
  buildFeatures { compose = true; buildConfig = true }
  defaultConfig {
    buildConfigField("String", "TOTERSLOKALISE_BASE_URL", "\"${demoValue("TOTERSLOKALISE_BASE_URL")}\"")
    buildConfigField("String", "TOTERSLOKALISE_SDK_TOKEN", "\"${demoValue("TOTERSLOKALISE_SDK_TOKEN")}\"")
    buildConfigField("String", "TOTERSLOKALISE_PROJECT_ID", "\"${demoValue("TOTERSLOKALISE_PROJECT_ID")}\"")
    buildConfigField("String", "TOTERSLOKALISE_ENVIRONMENT", "\"${demoValue("TOTERSLOKALISE_ENVIRONMENT").ifEmpty { "production" }}\"")
    buildConfigField("String", "TOTERSLOKALISE_DEFAULT_LOCALE", "\"${demoValue("TOTERSLOKALISE_DEFAULT_LOCALE").ifEmpty { "en" }}\"")
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

dependencies {
  implementation(project(":toterslokalise"))
  implementation("androidx.activity:activity-compose:1.10.1")
  implementation("androidx.compose.foundation:foundation:1.8.3")
  implementation("androidx.compose.material3:material3:1.3.2")
  implementation("androidx.compose.ui:ui:1.8.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}
