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
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))

    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("net.thauvin.erik:akismet-kotlin:0.9.1-beta")
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
