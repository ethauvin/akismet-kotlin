/*
 * AkismetBuild.java
 *
 * Copyright 2019-2023 Erik C. Thauvin (erik@thauvin.net)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.thauvin.erik;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.*;
import rife.bld.extension.dokka.DokkaOperation;
import rife.bld.extension.dokka.LoggingLevel;
import rife.bld.extension.dokka.OutputFormat;
import rife.bld.extension.dokka.SourceSet;
import rife.bld.operations.exceptions.ExitStatusException;
import rife.bld.publish.PomBuilder;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.*;

public class AkismetBuild extends Project {
    public AkismetBuild() {
        pkg = "net.thauvin.erik";
        name = "akismet-kotlin";
        version = version(1, 0, 1, "SNAPSHOT");

        javaRelease = 11;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL);

        var okHttp = version(4, 12, 0);
        scope(compile)
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib", version(1, 9, 21)))
                .include(dependency("com.squareup.okhttp3", "okhttp", okHttp))
                .include(dependency("com.squareup.okhttp3", "logging-interceptor", okHttp))
                .include(dependency("jakarta.servlet", "jakarta.servlet-api", version(6, 0, 0)))
                .include(dependency("org.jetbrains.kotlinx", "kotlinx-serialization-json-jvm", version(1, 6, 1)));
        scope(provided)
                .include(dependency("org.jetbrains.kotlin", "kotlin-serialization-compiler-plugin", version(1, 9, 21)));
        scope(test)
                .include(dependency("org.mockito", "mockito-core", version(5, 7, 0)))
                .include(dependency("org.jetbrains.kotlin", "kotlin-test-junit5", version(1, 9, 21)))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 10, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 10, 1)))
                .include(dependency("com.willowtreeapps.assertk", "assertk-jvm", version(0, 27, 0)));

        publishOperation()
                .repository(version.isSnapshot() ? repository(SONATYPE_SNAPSHOTS_LEGACY.location())
                        .withCredentials(property("sonatype.user"), property("sonatype.password"))
                        : repository(SONATYPE_RELEASES_LEGACY.location())
                        .withCredentials(property("sonatype.user"), property("sonatype.password")))
                .info()
                .groupId(pkg)
                .artifactId(name)
                .description("A client library for accessing the Automattic Kismet (Akismet) spam comments filtering service.")
                .url("https://github.com/ethauvin/" + name)
                .developer(
                        new PublishDeveloper()
                                .id("ethauvin")
                                .name("Erik C. Thauvin")
                                .email("erik@thauvin.net")
                                .url("https://erik.thauvin.net/")
                )
                .license(

                        new PublishLicense()
                                .name("BSD 3-Clause")
                                .url("https://opensource.org/licenses/BSD-3-Clause"))
                .scm(

                        new PublishScm()
                                .connection("scm:git:https://github.com/ethauvin/" + name + ".git")
                                .developerConnection("scm:git:git@github.com:ethauvin/" + name + ".git")
                                .url("https://github.com/ethauvin/" + name))
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));

        jarSourcesOperation().sourceDirectories(new File(srcMainDirectory(), "kotlin"));
    }

    public static void main(String[] args) {
        new AkismetBuild().start(args);
    }

    @BuildCommand(summary = "Compiles the Kotlin project")
    @Override
    public void compile() throws IOException {
        genver();
        new CompileKotlinOperation()
                .fromProject(this)
                .plugins(libCompileDirectory(), CompileKotlinPlugin.KOTLIN_SERIALIZATION)
                .execute();
    }

    @BuildCommand(summary = "Generates documentation in HTML format")
    public void docs() throws ExitStatusException, IOException, InterruptedException {
        var kotlin = new File(srcMainDirectory(), "kotlin").getAbsolutePath();
        new DokkaOperation()
                .fromProject(this)
                .loggingLevel(LoggingLevel.INFO)
                .moduleName("Akismet Kotlin")
                .moduleVersion(version.toString())
                .outputDir("docs")
                .outputFormat(OutputFormat.HTML)
                .sourceSet(
                        new SourceSet()
                                .src(kotlin)
                                .srcLink(kotlin, "https://github.com/ethauvin/" + name +
                                        "/tree/master/src/main/kotlin/", "#L")
                                .includes("config/dokka/packages.md")
                                .jdkVersion(javaRelease)
                )
                .execute();
    }

    @BuildCommand(summary = "Generates version class")
    public void genver() {
        new GeneratedVersionOperation()
                .fromProject(this)
                .projectName("Akismet Kotlin")
                .packageName(pkg + ".akismet")
                .classTemplate(new File(workDirectory(), "version.txt"))
                .directory(new File(srcMainDirectory(), "kotlin"))
                .extension(".kt")
                .execute();
    }

    @BuildCommand(summary = "Generates JaCoCo Reports")
    public void jacoco() throws IOException {
        new JacocoReportOperation()
                .fromProject(this)
                .execute();
    }

    @Override
    public void javadoc() throws ExitStatusException, IOException, InterruptedException {
        new DokkaOperation()
                .fromProject(this)
                .loggingLevel(LoggingLevel.INFO)
                .moduleName("Bitly Shorten")
                .moduleVersion(version.toString())
                .outputDir(new File(buildDirectory(), "javadoc"))
                .outputFormat(OutputFormat.JAVADOC)
                .execute();
    }

    @Override
    public void publish() throws Exception {
        super.publish();
        pomRoot();
    }

    @BuildCommand(value = "pom-root", summary = "Generates the POM file in the root directory")
    public void pomRoot() throws FileUtilsErrorException {
        PomBuilder.generateInto(publishOperation().fromProject(this).info(), dependencies(),
                new File(workDirectory, "pom.xml"));
    }
}
