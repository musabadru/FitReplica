plugins {
    id("fitreplica.android.library.compose")
    alias(libs.plugins.screenshot)
}

android {
    namespace = "com.fitreplica.core.designsystem"
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.compose.ui.tooling)
}
