plugins {
    id("fitreplica.android.library")
    id("fitreplica.android.room")
    id("fitreplica.android.hilt")
}

android {
    namespace = "com.fitreplica.core.database"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
}
