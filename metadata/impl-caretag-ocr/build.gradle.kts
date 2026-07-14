plugins {
    id("fitreplica.android.library")
}

android {
    namespace = "com.fitreplica.metadata.caretag"
}

dependencies {
    implementation(project(":metadata:api"))
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
}
