import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.Sign
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("maven-publish")
  id("signing")
}

// JitPack passes -Pgroup/-Pversion (see jitpack.yml); local builds use the defaults.
group = providers.gradleProperty("group").orNull ?: "com.toters.lokalise"
version = providers.gradleProperty("version").orNull ?: "1.2.0"

android {
  namespace = "com.toters.lokalise.sdk"
  compileSdk = 36

  defaultConfig {
    minSdk = 26
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}

publishing {
  publications {
    register<MavenPublication>("release") {
      groupId = project.group.toString()
      artifactId = "toterslokalise"
      version = project.version.toString()

      afterEvaluate {
        from(components["release"])
      }

      pom {
        name.set("TotersLokalise Android SDK")
        description.set(
          "Official TotersLokalise Android SDK for OTA localization."
        )
        url.set("https://arcade.stage.toters-api.com/toters-lokalise/")

        licenses {
          license {
            name.set("Apache License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
          }
        }

        developers {
          developer {
            id.set("toters")
            name.set("Toters Engineering")
          }
        }

        scm {
          connection.set(
            "scm:git:https://github.com/toters-eng/toterslokalise-android.git"
          )
          developerConnection.set(
            "scm:git:git@github.com:toters-eng/toterslokalise-android.git"
          )
          url.set("https://github.com/toters-eng/toterslokalise-android")
        }
      }
    }
  }

  repositories {
    maven {
      name = "localRelease"
      url = uri(layout.buildDirectory.dir("maven-repository"))
    }
    providers.environmentVariable("MAVEN_RELEASE_URL").orNull?.let { releaseUrl ->
      maven {
        name = "release"
        url = uri(releaseUrl)
        credentials {
          username = providers.environmentVariable("MAVEN_USERNAME").orNull
          password = providers.environmentVariable("MAVEN_PASSWORD").orNull
        }
      }
    }
  }
}

val releaseSigningEnabled = providers.gradleProperty("releaseSigning")
  .map(String::toBoolean)
  .orElse(false)

signing {
  isRequired = releaseSigningEnabled.get()
  if (releaseSigningEnabled.get()) {
    useGpgCmd()
    sign(publishing.publications["release"])
  }
}

tasks.withType<Sign>().configureEach {
  onlyIf { releaseSigningEnabled.get() }
}
