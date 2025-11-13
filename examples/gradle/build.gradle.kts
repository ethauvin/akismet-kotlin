plugins {
    id("application")
    id("com.github.ben-manes.versions") version "0.53.0"
    kotlin("jvm") version "2.2.21"
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

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
