import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.LinkMapping
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    jacoco
    java
    kotlin("jvm") version "1.3.50"
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.25.0"
    id("com.gradle.build-scan") version "2.4.2"
    id("com.jfrog.bintray") version "1.8.4"
    id("io.gitlab.arturbosch.detekt") version "1.0.1"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("org.jetbrains.dokka") version "0.9.18"
    id("org.jetbrains.kotlin.kapt").version("1.3.50")
    id("org.jmailen.kotlinter") version "2.1.1"
    id("org.sonarqube") version "2.7.1"
}

group = "net.thauvin.erik"
description = "Akismet for Kotlin/Java, a client library for accesssing the Automattic Kismet (Akismet) spam comments filtering service."

val gitHub = "ethauvin/$name"
val mavenUrl = "https://github.com/$gitHub"
val deployDir = "deploy"
var isRelease = "release" in gradle.startParameter.taskNames

var semverProcessor = "net.thauvin.erik:semver:1.2.0"

val publicationName = "mavenJava"

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
    implementation(semverProcessor)

    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("org.mockito:mockito-core:3.0.0")

    compile("com.squareup.okhttp3:okhttp:4.2.0")
    compile("com.squareup.okhttp3:logging-interceptor:4.2.0")

    compile(kotlin("stdlib"))

    testImplementation("org.testng:testng:7.0.0")
}

kapt {
    arguments {
        arg("semver.project.dir", projectDir)
    }
}

detekt {
    input = files("src/main/kotlin", "src/test/kotlin")
    filters = ".*/resources/.*,.*/build/.*"
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

    withType<KotlinCompile> {
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
        jdkVersion = 8
        val mapping = LinkMapping().apply {
            dir = "src/main/kotlin"
            url = "https://github.com/ethauvin/$name/blob/${project.version}/src/main/kotlin"
            suffix = "#L"
        }
        linkMappings = arrayListOf(mapping)
        includeNonPublic = false
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

    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
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
