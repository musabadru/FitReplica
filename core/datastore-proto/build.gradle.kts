plugins {
    id("fitreplica.jvm.library")
    alias(libs.plugins.protobuf)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobufJavalite.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                maybeCreate("java").option("lite")
                maybeCreate("kotlin").option("lite")
            }
        }
    }
}

dependencies {
    api(libs.protobuf.javalite)
    api(libs.protobuf.kotlin.lite)
}
