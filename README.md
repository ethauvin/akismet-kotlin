[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause)  
[![Known Vulnerabilities](https://snyk.io/test/github/ethauvin/akismet-kotlin/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/ethauvin/akismet-kotlin?targetFile=pom.xml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_akismet-kotlin&metric=alert_status)](https://sonarcloud.io/dashboard?id=ethauvin_akismet-kotlin) [![Build Status](https://travis-ci.org/ethauvin/akismet-kotlin.svg?branch=master)](https://travis-ci.org/ethauvin/akismet-kotlin) [![CircleCI](https://circleci.com/gh/ethauvin/akismet-kotlin/tree/master.svg?style=shield)](https://circleci.com/gh/ethauvin/akismet-kotlin/tree/master)

# [Akismet](https://www.akismet.com) for Kotlin/Java

Akismet for Kotlin/Java is a pretty complete and straightforward implementation of the [Automattic's Akismet](https://akismet.com/development/api/) API, a free service that can be used to actively stop comments spam.

## Examples (TL;DR)

#### Kotlin

```kotlin
val akismet = Akismet("YOUR_API_KEY", "YOUR_BLOG_URL")
val comment = AkismetComment(userIp = "127.0.0.1", userAgent = "curl/7.29.0")
              
with(comment) {
    referrer = "http://www.google.com"
    type = AkismetComment.TYPE_COMMENT
    author = "admin"
    authorEmail = "test@test.com"
    authorUrl = "http://www.CheckOutMyCoolSite.com"
    dateGmt = Akismet.dateToGmt(Date())
    content = "It means a lot that you would take the time to review our software."
}
// ...

val isSpam = akismet.checkComment(comment)
if (isSpam) {
    // ...
}
```

[View Full Example](https://github.com/ethauvin/akismet-kotlin/blob/master/examples/src/main/kotlin/com/example/AkismetExample.kt)

#### Java

```java
final Akismet akismet = new Akismet("YOUR_API_KEY", "YOUR_BLOG_URL");
final AkismetComment comment = new AkismetComment("127.0.0.1", "curl/7.29.0");

comment.setReferrer("http://www.google.com");
comment.setType(AkismetComment.TYPE_COMMENT);
comment.setAuthor("admin");
comment.setAuthorEmail("test@test.com");
comment.setAuthorUrl("http://www.CheckOutMyCoolSite.com");
comment.setDateGmt(Akismet.dateToGmt(new Date()));
comment.setContent("It means a lot that you would take the time to review our software.");
//...

final boolean isSpam = akismet.checkComment(comment);
if (isSpam) {
    // ...
}
```

[View Full Example](https://github.com/ethauvin/akismet-kotlin/blob/master/examples/src/main/java/com/example/AkismetSample.java)

### HttpServletRequest

The more information is sent to Akismet, the more accurate the response is. An [HttpServletRequest](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html) can be used as a parameter so that all of the relevant information is automatically included.

```kotlin
AkismetComment(request = context.getRequest())
```

[View Full Example](https://github.com/ethauvin/akismet-kotlin/blob/master/examples/src/main/kotlin/com/example/AkismetServlet.kt)

This will ensure that the user's IP, agent, referrer and various environment variables are automatically extracted from the request.

### JSON

Since comments mis-identified as spam or ham can be submitted to Askimet to improve the service. A comment can be saved as a JSON object to be stored in a database, etc.

```kotlin
var json = comment.toString()
```

At a latter time, the comment can the be submitted:

```kotlin
akismet.submitSpam(Akismet.jsonComment(json))
```

