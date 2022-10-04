import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.github.ben-manes.versions") version "0.42.0"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("java-library")
    id("java")
    id("maven-publish")
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("org.jetbrains.dokka") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.6.0"
    id("org.sonarqube") version "3.4.0.2513"
    id("signing")
    kotlin("jvm") version "1.7.20"
    kotlin("kapt") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
}

group = "net.thauvin.erik"
description = "Akismet for Kotlin/Java, a client library for accessing the Automattic Kismet (Akismet) spam comments filtering service."

val gitHub = "ethauvin/$name"
val mavenUrl = "https://github.com/$gitHub"
val deployDir = "deploy"
var isRelease = "release" in gradle.startParameter.taskNames

var semverProcessor = "net.thauvin.erik:semver:1.2.0"

val publicationName = "mavenJava"

object Versions {
    const val OKHTTP = "4.10.0"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    kapt(semverProcessor)
    compileOnly(semverProcessor)

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

//    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")


    implementation("com.squareup.okhttp3:okhttp:${Versions.OKHTTP}")
    implementation("com.squareup.okhttp3:logging-interceptor:${Versions.OKHTTP}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.testng:testng:7.6.1")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")

}

kapt {
    arguments {
        arg("semver.project.dir", projectDir)
    }
}

detekt {
    //toolVersion = "main-SNAPSHOT"
    baseline = project.rootDir.resolve("config/detekt/baseline.xml")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

sonarqube {
    properties {
        property("sonar.projectKey", "ethauvin_$name")
        property("sonar.organization", "ethauvin-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.buildDir}/reports/kover/xml/report.xml")
    }
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    description = "Assembles a JAR of the generated Javadoc."
    group = JavaBasePlugin.DOCUMENTATION_GROUP
}

tasks {
    withType<Test> {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }

        useTestNG()
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = java.targetCompatibility.toString()
    }

    withType<GenerateMavenPom> {
        destination = file("$projectDir/pom.xml")
    }

    withType<DependencyUpdatesTask> {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }

    assemble {
        dependsOn(javadocJar)
    }

    clean {
        doLast {
            project.delete(fileTree(deployDir))
        }
    }

    dokkaHtml {
        outputDirectory.set(file("$projectDir/docs"))

        dokkaSourceSets {
            configureEach {
                includes.from("config/dokka/packages.md")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin/"))
                    remoteUrl.set(URL("https://github.com/ethauvin/${project.name}/tree/master/src/main/kotlin/"))
                    remoteLineSuffix.set("#L")
                }
                externalDocumentationLink {
                    url.set(URL("https://jakarta.ee/specifications/platform/9/apidocs/"))
                }
            }
        }
    }

    dokkaJavadoc {
        dokkaSourceSets {
            configureEach {
                includes.from("config/dokka/packages.md")
                externalDocumentationLink {
                    url.set(URL("https://jakarta.ee/specifications/platform/9/apidocs/"))
                }
            }
        }
    }

    val copyToDeploy by registering(Copy::class) {
        from(configurations.runtimeClasspath) {
            exclude("annotations-*.jar", "jakarta.servlet-*.jar")
        }
        from(jar)
        into(deployDir)
    }

    register("deploy") {
        description = "Copies all needed files to the $deployDir directory."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(clean, wrapper, build, jar)
        outputs.dir(deployDir)
        inputs.files(copyToDeploy)
        mustRunAfter(clean)
    }

    val gitIsDirty by registering(Exec::class) {
        description = "Fails if git has uncommitted changes."
        group = "verification"
        commandLine("git", "diff", "--quiet", "--exit-code")
    }

    val gitTag by registering(Exec::class) {
        description = "Tags the local repository with version ${project.version}"
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(gitIsDirty)
        if (isRelease) {
            commandLine("git", "tag", "-a", project.version, "-m", "Version ${project.version}")
        }
    }

    register("release") {
        description = "Publishes version ${project.version} to local repository."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(wrapper, "deploy", gitTag, publishToMavenLocal)
    }

    "sonarqube" {
        dependsOn(koverReport)
    }
}

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            from(components["java"])
            artifact(javadocJar)
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set(mavenUrl)
                licenses {
                    license {
                        name.set("BSD 3-Clause")
                        url.set("https://opensource.org/licenses/BSD-3-Clause")
                    }
                }
                developers {
                    developer {
                        id.set("ethauvin")
                        name.set("Erik C. Thauvin")
                        email.set("erik@thauvin.net")
                        url.set("https://erik.thauvin.net/")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/$gitHub.git")
                    developerConnection.set("scm:git:git@github.com:$gitHub.git")
                    url.set(mavenUrl)
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("$mavenUrl/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"
            project.afterEvaluate {
                url = if (project.version.toString().contains("SNAPSHOT"))
                    uri("https://oss.sonatype.org/content/repositories/snapshots/") else
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications[publicationName])
}
