import java.util.Properties

plugins {
    id("fitreplica.android.application")
    id("fitreplica.android.application.compose")
    id("fitreplica.android.hilt")
}

val versionProps =
    Properties().apply {
        load(rootProject.file("version.properties").inputStream())
    }
// VERSION_NAME carries a trailing "# x-release-please-version" marker (release-please's generic
// updater requires the marker on the same line as the value it rewrites), so only the leading
// semver token is the real version — java.util.Properties has no mid-line comment syntax to strip it.
val releaseVersionName =
    Regex("""\d+\.\d+\.\d+""").find(versionProps.getProperty("VERSION_NAME"))?.value
        ?: error("version.properties VERSION_NAME must contain a semantic version")
val (verMajor, verMinor, verPatch) = releaseVersionName.split(".").map { it.toInt() }
require(verMinor in 0..999 && verPatch in 0..999) {
    "minor/patch must stay under 1000 to keep versionCode packing collision-free: $releaseVersionName"
}
// Wide packing (minor/patch capped at 3 digits each) so e.g. 0.1.100 and 0.2.0 can't collide.
val releaseVersionCode = verMajor * 1_000_000 + verMinor * 1_000 + verPatch

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }

fun signingProperty(key: String): String? = localProperties.getProperty(key) ?: System.getenv(key)
val releaseStoreFile = signingProperty("RELEASE_STORE_FILE")

android {
    namespace = "com.fitreplica.app"

    defaultConfig {
        applicationId = "com.fitreplica.app"
        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    signingConfigs {
        create("release") {
            if (releaseStoreFile != null) {
                storeFile = file(releaseStoreFile)
                storePassword = signingProperty("RELEASE_STORE_PASSWORD")
                keyAlias = signingProperty("RELEASE_KEY_ALIAS")
                keyPassword = signingProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (releaseStoreFile != null) signingConfigs.getByName("release") else null
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))

    implementation(project(":feature:closet"))
    implementation(project(":feature:outfit"))
    implementation(project(":feature:history"))
    implementation(project(":feature:laundry"))
    implementation(project(":feature:analytics"))

    implementation(project(":avatar:api"))
    implementation(project(":metadata:api"))
    implementation(project(":metadata:impl-noop"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
}
