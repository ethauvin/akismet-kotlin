plugins {
  id("com.gradle.enterprise").version("3.1.1")
}

rootProject.name = "akismet-kotlin"

gradleEnterprise {
    buildScan {
        // plugin configuration
    }
}
