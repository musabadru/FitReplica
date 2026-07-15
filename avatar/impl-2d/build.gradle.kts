plugins {
    id("fitreplica.android.library.compose")
    id("fitreplica.android.hilt")
}

android {
    namespace = "com.fitreplica.avatar.impl2d"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":avatar:api"))
    implementation(project(":core:model"))

    testImplementation(libs.junit)
}
