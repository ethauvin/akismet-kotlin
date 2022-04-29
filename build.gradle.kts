import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.github.ben-manes.versions") version "0.42.0"
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    id("java-library")
    id("java")
    id("maven-publish")
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("org.jetbrains.dokka") version "1.6.21"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    id("org.sonarqube") version "3.3"
    id("signing")
    kotlin("jvm") version "1.6.21"
    kotlin("kapt") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
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
    const val OKHTTP = "4.9.3"
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

    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("com.squareup.okhttp3:okhttp:${Versions.OKHTTP}")
    implementation("com.squareup.okhttp3:logging-interceptor:${Versions.OKHTTP}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("org.testng:testng:7.5")
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

sonarqube {
    properties {
        property("sonar.projectKey", "ethauvin_$name")
        property("sonar.organization", "ethauvin-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.buildDir}/reports/kover/report.xml")
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
                jdkVersion.set(8)
                includes.from("config/dokka/packages.md")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin/"))
                    remoteUrl.set(URL("https://github.com/ethauvin/${project.name}/tree/master/src/main/kotlin/"))
                    remoteLineSuffix.set("#L")
                }
                externalDocumentationLink {
                    url.set(URL("https://javaee.github.io/javaee-spec/javadocs/"))
                    packageListUrl.set(URL("https://javaee.github.io/javaee-spec/javadocs/package-list"))
                }
            }
        }
    }

    dokkaJavadoc {
        dokkaSourceSets {
            configureEach {
                jdkVersion.set(8)
                includes.from("config/dokka/packages.md")
                externalDocumentationLink {
                    url.set(URL("https://javaee.github.io/javaee-spec/javadocs/"))
                    packageListUrl.set(URL("https://javaee.github.io/javaee-spec/javadocs/package-list"))
                }
            }
        }
        dependsOn(dokkaHtml)
    }

    val copyToDeploy by registering(Copy::class) {
        from(configurations.runtimeClasspath) {
            exclude("annotations-*.jar")
        }
        from(jar)
        into(deployDir)
    }

    register("deploy") {
        description = "Copies all needed files to the $deployDir directory."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(build, jar)
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
