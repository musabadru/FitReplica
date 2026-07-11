plugins {
    id("fitreplica.jvm.library")
}

dependencies {
    implementation(project(":metadata:api"))
    implementation(libs.javax.inject)
}
