plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("com.github.ben-manes.versions") version "0.38.0"
    application
}

// ./gradlew run runJava

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("net.thauvin.erik:akismet-kotlin:0.9.3")
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
