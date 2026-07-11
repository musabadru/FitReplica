plugins {
    id("fitreplica.android.library")
    id("fitreplica.android.hilt")
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.fitreplica.core.datastore"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobufJavalite.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.datastore.core)
    implementation(libs.datastore)
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)
}

// KSP (Hilt) resolves the generated `UserPreferences` proto class from the Kotlin
// source set, but has no inherent ordering against protobuf codegen — without this,
// kspDebugKotlin can run before generateDebugProto and fail with error.NonExistentClass.
androidComponents {
    onVariants(selector().all()) { variant ->
        val capitalizedName = variant.name.replaceFirstChar { it.uppercase() }
        afterEvaluate {
            tasks.named("ksp${capitalizedName}Kotlin") {
                dependsOn("generate${capitalizedName}Proto")
            }
        }
    }
}
