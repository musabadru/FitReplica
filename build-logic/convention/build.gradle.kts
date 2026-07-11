plugins {
    `kotlin-dsl`
}

group = "com.fitreplica.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.protobuf.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "fitreplica.android.application"
            implementationClass = "com.fitreplica.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "fitreplica.android.application.compose"
            implementationClass = "com.fitreplica.buildlogic.AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "fitreplica.android.library"
            implementationClass = "com.fitreplica.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "fitreplica.android.library.compose"
            implementationClass = "com.fitreplica.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "fitreplica.android.feature"
            implementationClass = "com.fitreplica.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "fitreplica.android.hilt"
            implementationClass = "com.fitreplica.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "fitreplica.android.room"
            implementationClass = "com.fitreplica.buildlogic.AndroidRoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "fitreplica.jvm.library"
            implementationClass = "com.fitreplica.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
