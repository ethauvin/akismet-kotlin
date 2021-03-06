plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
    id("com.github.ben-manes.versions") version "0.29.0"
    application
}

// ./gradlew run runJava

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("net.thauvin.erik:akismet-kotlin:0.9.2")
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
