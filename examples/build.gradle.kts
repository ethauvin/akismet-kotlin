plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    application
}

// ./gradlew run runJava

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation("net.thauvin.erik:akismet-kotlin:0.9.0-beta")
}

application {
    mainClassName = "com.example.AkismetExampleKt"
}

tasks {
    register("runJava", JavaExec::class) {
        group = "application"
        main = "com.example.AkismetSample"
        classpath = sourceSets["main"].runtimeClasspath
    }
}
