import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.net.URL
import java.util.Properties

plugins {
    jacoco
    java
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.28.0"
    id("com.jfrog.bintray") version "1.8.4"
    id("io.gitlab.arturbosch.detekt") version "1.6.0"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("org.jetbrains.kotlin.kapt") version "1.3.70"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.70"
    id("org.jmailen.kotlinter") version "2.3.2"
    id("org.sonarqube") version "2.8"
}

group = "net.thauvin.erik"
description = "Akismet for Kotlin/Java, a client library for accessing the Automattic Kismet (Akismet) spam comments filtering service."

val gitHub = "ethauvin/$name"
val mavenUrl = "https://github.com/$gitHub"
val deployDir = "deploy"
var isRelease = "release" in gradle.startParameter.taskNames

var semverProcessor = "net.thauvin.erik:semver:1.2.0"

val publicationName = "mavenJava"

object VersionInfo {
    const val okhttp = "4.4.0"
}

val versions: VersionInfo by extra { VersionInfo }

// Load local.properties
File("local.properties").apply {
    if (exists()) {
        FileInputStream(this).use { fis ->
            Properties().apply {
                load(fis)
                forEach { (k, v) ->
                    extra[k as String] = v
                }
            }
        }
    }
}

repositories {
    jcenter()
}

dependencies {
    kapt(semverProcessor)
    compileOnly(semverProcessor)

    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("com.squareup.okhttp3:okhttp:${versions.okhttp}")
    implementation("com.squareup.okhttp3:logging-interceptor:${versions.okhttp}")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0-1.3.70-eap-274-2")

    testImplementation("org.mockito:mockito-core:3.3.1")
    testImplementation("org.testng:testng:7.1.1")
}

kapt {
    arguments {
        arg("semver.project.dir", projectDir)
    }
}

detekt {
    //input = files("src/main/kotlin", "src/test/kotlin")
    //filters = ".*/resources/.*,.*/build/.*"
    baseline = project.rootDir.resolve("detekt-baseline.xml")
}

kotlinter {
    ignoreFailures = false
    reporters = arrayOf("html")
    experimentalRules = false
    disabledRules = arrayOf("import-ordering")
}

jacoco {
    toolVersion = "0.8.3"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sonarqube {
    properties {
        property("sonar.projectKey", "ethauvin_$name")
        property("sonar.sourceEncoding", "UTF-8")
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokka)
    from(tasks.dokka)
    archiveClassifier.set("javadoc")
    description = "Assembles a JAR of the generated Javadoc."
    group = JavaBasePlugin.DOCUMENTATION_GROUP
}

val dokkaDocs by tasks.creating(DokkaTask::class) {
    outputFormat = "gfm"
    outputDirectory = "$projectDir"

    configuration {
        moduleName = "docs"
        sourceLink {
            path = file("$projectDir/src/main/kotlin").toURI().toString().replace("file:", "")
            url = "https://github.com/ethauvin/${project.name}/tree/master/src/main/kotlin"
            lineSuffix = "#L"
        }

        jdkVersion = 8

        externalDocumentationLink {
            url = URL("https://javaee.github.io/javaee-spec/javadocs/")
            packageListUrl = URL("https://javaee.github.io/javaee-spec/javadocs/package-list")
        }

        includes = listOf("config/dokka/packages.md")
        includeNonPublic = false
    }
}

tasks {
    withType<Test> {
        useTestNG()
    }

    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<GenerateMavenPom> {
        destination = file("$projectDir/pom.xml")
    }

    assemble {
        dependsOn(sourcesJar, javadocJar)
    }

    clean {
        doLast {
            project.delete(fileTree(deployDir))
        }
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"

        configuration {
            sourceLink {
                path = file("$projectDir/src/main/kotlin").toURI().toString().replace("file:", "")
                url = "https://github.com/ethauvin/${project.name}/tree/master/src/main/kotlin"
                lineSuffix = "#L"
            }

            jdkVersion = 8

            externalDocumentationLink {
                url = URL("https://javaee.github.io/javaee-spec/javadocs/")
                packageListUrl = URL("https://javaee.github.io/javaee-spec/javadocs/package-list")
            }

            includes = listOf("config/dokka/packages.md")
            includeNonPublic = false
        }
        dependsOn(dokkaDocs)
    }

    val copyToDeploy by registering(Copy::class) {
        from(configurations.runtime) {
            exclude("annotations-*.jar")
        }
        from(jar)
        into(deployDir)
    }

    register("deploy") {
        description = "Copies all needed files to the $deployDir directory."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn("build", "jar")
        outputs.dir(deployDir)
        inputs.files(copyToDeploy)
        mustRunAfter("clean")
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

    val bintrayUpload by existing(BintrayUploadTask::class) {
        dependsOn(publishToMavenLocal, gitTag)
    }

    register("release") {
        description = "Publishes version ${project.version} to Bintray."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn("wrapper", bintrayUpload)
    }

    "sonarqube" {
        dependsOn("jacocoTestReport")
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?
bintray {
    user = findProperty("bintray.user")
    key = findProperty("bintray.apikey")
    publish = isRelease
    setPublications(publicationName)
    pkg.apply {
        repo = "maven"
        name = project.name
        desc = description
        websiteUrl = mavenUrl
        issueTrackerUrl = "$mavenUrl/issues"
        githubRepo = gitHub
        githubReleaseNotesFile = "README.md"
        vcsUrl = "$mavenUrl.git"
        setLabels("kotlin", "java", "akismet", "comments", "spam", "blog", "automattic", "kismet")
        publicDownloadNumbers = true
        version.apply {
            name = project.version as String
            desc = description
            vcsTag = project.version as String
            gpg.apply {
                sign = true
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            pom.withXml {
                asNode().apply {
                    appendNode("name", project.name)
                    appendNode("description", project.description)
                    appendNode("url", mavenUrl)

                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", "BSD 3-Clause")
                        appendNode("url", "https://opensource.org/licenses/BSD-3-Clause")
                    }

                    appendNode("developers").appendNode("developer").apply {
                        appendNode("id", "ethauvin")
                        appendNode("name", "Erik C. Thauvin")
                        appendNode("email", "erik@thauvin.net")
                    }

                    appendNode("scm").apply {
                        appendNode("connection", "scm:git:$mavenUrl.git")
                        appendNode("developerConnection", "scm:git:git@github.com:$gitHub.git")
                        appendNode("url", mavenUrl)
                    }

                    appendNode("issueManagement").apply {
                        appendNode("system", "GitHub")
                        appendNode("url", "$mavenUrl/issues")
                    }
                }
            }
        }
    }
}
