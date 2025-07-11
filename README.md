[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](https://opensource.org/licenses/BSD-3-Clause)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-7f52ff)](https://kotlinlang.org/)
[![bld](https://img.shields.io/badge/2.3.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://img.shields.io/github/release/ethauvin/akismet-kotlin.svg)](https://github.com/ethauvin/akismet-kotlin/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/net.thauvin.erik/akismet-kotlin.svg?color=blue)](https://central.sonatype.com/artifact/net.thauvin.erik/akismet-kotlin)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fnet%2Fthauvin%2Ferik%2Fakismet-kotlin%2Fmaven-metadata.xml&label=snapshot)](https://github.com/ethauvin/akismet-kotlin/packages/2260603/versions)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_akismet-kotlin&metric=alert_status)](https://sonarcloud.io/dashboard?id=ethauvin_akismet-kotlin)
[![GitHub CI](https://github.com/ethauvin/akismet-kotlin/actions/workflows/bld.yml/badge.svg)](https://github.com/ethauvin/akismet-kotlin/actions/workflows/bld.yml)
[![CircleCI](https://circleci.com/gh/ethauvin/akismet-kotlin/tree/master.svg?style=shield)](https://circleci.com/gh/ethauvin/akismet-kotlin/tree/master)

# [Akismet](https://www.akismet.com) for Kotlin, Java and Android

A pretty complete and straightforward implementation of the [Automattic's Akismet](https://akismet.com/development/api/) API, a free service which can be used to actively stop comments spam.

## Examples (TL;DR)

### Kotlin

```kotlin
val akismet = Akismet(apiKey = "YOUR_API_KEY", blog = "YOUR_BLOG_URL")
val comment = AkismetComment(userIp = "127.0.0.1", userAgent = "curl/7.29.0").apply {
    referrer = "https://www.google.com"
    type = CommentType.COMMENT
    author = "admin"
    authorEmail = "test@test.com"
    authorUrl = "https://www.CheckOutMyCoolSite.com"
    dateGmt = Akismet.dateToGmt(Date())
    content = "Thanks for reviewing our software."
}
// ...

val isSpam = akismet.checkComment(comment)
if (isSpam) {
    // ...
}
```

[View Full Examples](https://github.com/ethauvin/akismet-kotlin/blob/master/examples)

### Java

```java
final Akismet akismet = new Akismet("YOUR_API_KEY", "YOUR_BLOG_URL");
final AkismetComment comment = new AkismetComment(
    new CommentConfig.Builder("127.0.0.1", "curl/7.29.0")
            .referrer("https://www.google.com")
            .type(CommentType.COMMENT)
            .author("admin")
            .authorEmail("test@test.com")
            .authorUrl("https://www.CheckOutMyCoolSite.com")
            .dateGmt(Akismet.dateToGmt(new Date()))
            .content("Thanks for reviewing our software.")
            .build()
);
//...

final boolean isSpam = akismet.checkComment(comment);
if (isSpam) {
    // ...
}
```

[View Full Examples](https://github.com/ethauvin/akismet-kotlin/blob/master/examples)

## bld

To use with [bld](https://rife2.com/bld), include the following dependency in your [build](https://github.com/ethauvin/akismet-kotlin/blob/master/examples/bld/src/bld/java/com/example/ExampleBuild.java) file:

```java
repositories = List.of(MAVEN_CENTRAL, CENTRAL_SNAPSHOTS);

scope(compile)
    .include(dependency("net.thauvin.erik:akismet-kotlin:1.1.0-SNAPSHOT"));
```

## Gradle

To use with [Gradle](https://gradle.org/), include the following dependency in your [build](https://github.com/ethauvin/akismet-kotlin/blob/master/examples/gradle/build.gradle.kts) file:

```gradle
repositories {
    maven {
        name = 'Central Portal Snapshots'
        url = 'https://central.sonatype.com/repository/maven-snapshots/'
    }
    mavenCentral()
}

dependencies {
    implementation("net.thauvin.erik:akismet-kotlin:1.1.0-SNAPSHOT")
}
```

Instructions for using with Maven, Ivy, etc. can be found on [Maven Central](https://central.sonatype.com/artifact/net.thauvin.erik/akismet-kotlin).

## HttpServletRequest

The more information is sent to Akismet, the more accurate the response is. An [HttpServletRequest](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html) can be used as a parameter so that all the relevant information is automatically included.

```kotlin
AkismetComment(request = context.getRequest())
```

This will ensure that the user's IP, agent, referrer and various environment variables are automatically extracted from the request.

[View Full Example](https://github.com/ethauvin/akismet-kotlin/blob/master/examples/gradle/src/main/kotlin/com/example/AkismetServlet.kt)

## JSON

Since comments mis-identified as spam or ham can be submitted to Askimet to improve the service. A comment can be saved as a JSON object to be stored in a database, etc.

```kotlin
var json = comment.toJson()
```

At a latter time, the comment can then be submitted:

```kotlin
akismet.submitSpam(Akismet.jsonComment(json))
```

## Contributing

If you want to contribute to this project, all you have to do is clone the GitHub
repository:

```console
git clone git@github.com:ethauvin/akismet-kotlin.git
```

Then use [bld](https://rife2.com/bld) to build:

```console
cd akismet-kotlin
./bld compile
```

The project has an [IntelliJ IDEA](https://www.jetbrains.com/idea/) project structure. You can just open it after all the dependencies were downloaded and peruse the code.

### More…

If all else fails, there's always more [Documentation](https://ethauvin.github.io/akismet-kotlin/).
