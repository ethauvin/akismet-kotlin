import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("com.github.ben-manes.versions") version "0.48.0"
    kotlin("jvm") version "1.9.10"
}

// ./gradlew run --args=API_KEY
// ./gradlew runJava --args=API_KEY

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
//    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

    implementation("net.thauvin.erik:akismet-kotlin:1.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("com.example.AkismetExampleKt")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = java.targetCompatibility.toString()
    }

    register("runJava", JavaExec::class) {
        group = "application"
        mainClass.set("com.example.AkismetSample")
        classpath = sourceSets.main.get().runtimeClasspath
    }
}
