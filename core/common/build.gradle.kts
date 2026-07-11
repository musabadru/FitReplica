plugins {
    id("fitreplica.jvm.library")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    api(libs.javax.inject)
}
