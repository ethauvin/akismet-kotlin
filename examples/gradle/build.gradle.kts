plugins {
    id("application")
    id("io.github.ben-manes.versions") version "0.60.0"
    kotlin("jvm") version "2.4.20"
}

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    implementation("net.thauvin.erik:akismet-kotlin:1.1.0-SNAPSHOT")
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
