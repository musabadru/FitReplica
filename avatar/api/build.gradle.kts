plugins {
    id("fitreplica.android.library.compose")
}

android {
    namespace = "com.fitreplica.avatar.api"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
}
