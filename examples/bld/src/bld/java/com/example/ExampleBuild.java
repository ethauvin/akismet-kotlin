package com.example;

import rife.bld.BaseProject;
import rife.bld.BuildCommand;
import rife.bld.extension.CompileKotlinOperation;
import rife.bld.operations.RunOperation;

import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;

public class ExampleBuild extends BaseProject {
    public ExampleBuild() {
        pkg = "com.example";
        name = "Example";
        version = version(0, 1, 0);

        mainClass = "com.example.AkismetExampleKt";

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL, CENTRAL_SNAPSHOTS);

        scope(compile)
                .include(dependency("net.thauvin.erik:akismet-kotlin:1.1.0-SNAPSHOT"))
                .include(dependency("jakarta.servlet:jakarta.servlet-api:6.1.0"));
    }

    public static void main(String[] args) {
        new ExampleBuild().start(args);
    }

    @Override
    public void compile() throws Exception {
        new CompileKotlinOperation()
                .fromProject(this)
                .execute();

        // Also compile the Java source code
        super.compile();
    }

    @BuildCommand(value = "run-java", summary = "Runs the Java example")
    public void runJava() throws Exception {
        new RunOperation()
                .fromProject(this)
                .mainClass("com.example.AkismetSample")
                .execute();
    }
}
