plugins {
    id("fitreplica.android.feature")
}

android {
    namespace = "com.fitreplica.feature.closet"
}

dependencies {
    // Only :feature:closet needs these so far: activity-compose for the camera-capture/
    // photo-picker ActivityResultContracts launchers, coil-compose to render photos —
    // neither is pulled in by the shared feature convention plugin.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.compose.material.icons.extended)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
