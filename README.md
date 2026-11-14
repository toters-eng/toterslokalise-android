# TotersLokalise Android SDK

The Android/Kotlin library implements TotersLokalise OTA protocol v1 for Android API
26+. It is distributed from this public GitHub repository through
[JitPack](https://jitpack.io), which builds each git tag.

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
  }
}
```

Module `build.gradle.kts` (use the release tag, e.g. `1.2.0`):

```kotlin
implementation("com.github.toters-eng.toterslokalise-android:toterslokalise:1.2.0")
```

Build status and logs: `https://jitpack.io/#toters-eng/toterslokalise-android`.

### Build-only namespaces

Namespaces marked **Build only** in TotersLokalise are left out of OTA releases.
Download them in your CI/CD pipeline (one `<namespace>.json` per namespace, from
`GET /api/v1/projects/{projectId}/translations?environment=production&namespace=<name>&format=json`;
ready-made Bitbucket and GitHub Actions steps are on the Developer Hub) and bundle them
with the app. Lookups fall back to these bundled strings.

Write the files to `src/main/assets/toterslokalise`, then:

```kotlin
val bundled = ExportedBundledTranslations.fromAssets(context.assets, "toterslokalise")
```

## Releasing

Releases are plain semantic-version git tags (`1.0.1`, no `v`). Merge to `main`,
then:

```bash
git tag 1.0.1
git push origin 1.0.1
```

The Release workflow runs the unit tests on the tag, creates the GitHub release
and asks JitPack to build the tag, so the version is ready before an app needs
it.

Create one long-lived client:

```kotlin
val client = TotersLokaliseClient(
  TotersLokaliseConfiguration(
    baseUrl = URL("https://arcade.stage.toters-api.com/toters-lokalise/api"),
    token = "gl_sdk_your_public_read_token",
    projectId = "your-project-id",
    environment = "production",
    locale = "ar",
    cache = FileCacheAdapter(File(context.cacheDir, "toterslokalise")),
    bundled = AndroidResourcesBundledTranslations(
      context.resources,
      mapOf("common.welcome" to R.string.common_welcome),
    ),
  ),
)

lifecycleScope.launch { client.initialize() }

// Always synchronous: OTA memory → Android string resource → fallback → key.
val title = client.translation("welcome", namespace = "common", fallback = "Welcome")
```

For resource fallback, provide an explicit key-to-resource-ID map as shown. This
keeps resource references type-safe and visible to Android's resource shrinker.
`initialize()`, `refresh()`, and `setLocale()` are suspend functions. Lookup never
performs network or disk I/O.

The client verifies scope, protocol, origin, byte size, and SHA-256 before an
atomic cache replacement. Failures preserve last-known-good OTA content and
bundled resources. The runnable `:sample` module is a configuration-driven Compose
demo with release status, supported-locale switching, search, namespace filtering,
explicit refresh, `LazyColumn` rendering, and RTL support.
