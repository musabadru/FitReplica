plugins {
    id("fitreplica.android.feature")
}

android {
    namespace = "com.fitreplica.feature.outfit"
}

dependencies {
    implementation(project(":avatar:api"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
