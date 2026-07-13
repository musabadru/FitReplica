plugins {
    id("fitreplica.android.feature")
}

android {
    namespace = "com.fitreplica.feature.history"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
