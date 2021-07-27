plugins {
    id("application")
    id("com.github.ben-manes.versions") version "0.39.0"
    kotlin("jvm") version "1.5.21"
}

// ./gradlew run runJava

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("net.thauvin.erik:akismet-kotlin:0.9.3-SNAPSHOT")
}

application {
    mainClass.set("com.example.AkismetExampleKt")
}

tasks {
    register("runJava", JavaExec::class) {
        group = "application"
        mainClass.set("com.example.AkismetSample")
        classpath = sourceSets.main.get().runtimeClasspath
    }
}
