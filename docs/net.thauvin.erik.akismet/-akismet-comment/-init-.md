[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [AkismetComment](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`AkismetComment(request: `[`HttpServletRequest`](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html)`)`

Create an Akismet comment extracting the [userIp](user-ip.md), [userAgent](user-agent.md), [referrer](referrer.md) and [serverEnv](server-env.md) environment variables
from a Servlet request.

See the
[Akismet API](https://akismet.com/development/api/#comment-check) for more details.

**See Also**

[serverEnv](server-env.md)

`AkismetComment(userIp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, userAgent: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`

Create an Akismet comment instance.

See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.

### Parameters

`userIp` - IP address of the comment submitter.

`userAgent` - User agent string of the web browser submitting the comment.

**Constructor**

Create an Akismet comment instance.



See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.

