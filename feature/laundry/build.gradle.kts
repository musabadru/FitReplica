plugins {
    id("fitreplica.android.feature")
}

android {
    namespace = "com.fitreplica.feature.laundry"
}

dependencies {
    implementation(project(":metadata:api"))
}
