plugins {
    id("fitreplica.android.application")
    id("fitreplica.android.application.compose")
    id("fitreplica.android.hilt")
}

android {
    namespace = "com.fitreplica.app"

    defaultConfig {
        applicationId = "com.fitreplica.app"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
