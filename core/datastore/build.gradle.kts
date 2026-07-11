plugins {
    id("fitreplica.android.library")
    id("fitreplica.android.hilt")
}

android {
    namespace = "com.fitreplica.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:datastore-proto"))

    implementation(libs.datastore.core)
    implementation(libs.datastore)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
