[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [AkismetComment](./index.md)

# AkismetComment

`open class AkismetComment` [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/AkismetComment.kt#L59)

A comment to send to Akismet.

Most everything is optional. Performance can drop dramatically if you choose to exclude data points. The more data
you send Akismet about each comment, the greater the accuracy. They recommend erring on the side of including
too much data.

See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | Create an Akismet comment extracting the [userIp](user-ip.md), [userAgent](user-agent.md), [referrer](referrer.md) and [serverEnv](server-env.md) environment variables from a Servlet request.`AkismetComment(request: `[`HttpServletRequest`](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html)`)`<br>Create an Akismet comment instance.`AkismetComment(userIp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, userAgent: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)` |

### Properties

| Name | Summary |
|---|---|
| [author](author.md) | Name submitted with the comment.`var author: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [authorEmail](author-email.md) | Email address submitted with the comment.`var authorEmail: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [authorUrl](author-url.md) | URL submitted with comment.`var authorUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [blogCharset](blog-charset.md) | The character encoding for the form values included in comment parameters, such as UTF-8 or ISO-8859-1`var blogCharset: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [blogLang](blog-lang.md) | Indicates the language(s) in use on the blog or site, in ISO 639-1 format, comma-separated.`var blogLang: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [content](content.md) | The content that was submitted.`var content: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [dateGmt](date-gmt.md) | The UTC timestamp of the creation of the comment, in ISO 8601 format.`var dateGmt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [isTest](is-test.md) | This is an optional parameter. You can use it when submitting test queries to Akismet.`var isTest: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [permalink](permalink.md) | The full permanent URL of the entry the comment was submitted to.`var permalink: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [postModifiedGmt](post-modified-gmt.md) | The UTC timestamp of the publication time for the post, page or thread on which the comment was posted.`var postModifiedGmt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [recheckReason](recheck-reason.md) | If you are sending content to Akismet to be rechecked, such as a post that has been edited or old pending comments that you'd like to recheck, include this parameter with a string describing why the content is being rechecked.`var recheckReason: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [referrer](referrer.md) | The content of the referer header should be set here.`var referrer: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [serverEnv](server-env.md) | In PHP, there is an array of environmental variables called `$_SERVER` that contains information about the Web server itself as well as a key/value for every HTTP header sent with the request. This data is highly useful to Akismet.`var serverEnv: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |
| [type](type.md) | A string that describes the type of content being sent, such as:`var type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [userAgent](user-agent.md) | User agent string of the web browser submitting the comment.`val userAgent: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [userIp](user-ip.md) | IP address of the comment submitter.`val userIp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [userRole](user-role.md) | The user role of the user who submitted the comment. This is an optional parameter.`var userRole: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |

### Functions

| Name | Summary |
|---|---|
| [equals](equals.md) | Indicates whether some other object is *equal to* this one.`open fun equals(other: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [hashCode](hash-code.md) | Returns a hash code value for the object.`open fun hashCode(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [toJson](to-json.md) | Returns a JSON representation of the comment.`fun toJson(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [toString](to-string.md) | Returns a JSON representation of the comment.`open fun toString(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Companion Object Properties

| Name | Summary |
|---|---|
| [ADMIN_ROLE](-a-d-m-i-n_-r-o-l-e.md) | Administrator role. If used, Akismet will always return false.`const val ADMIN_ROLE: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_BLOG_POST](-t-y-p-e_-b-l-o-g_-p-o-s-t.md) | A blog post.`const val TYPE_BLOG_POST: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_COMMENT](-t-y-p-e_-c-o-m-m-e-n-t.md) | A blog comment.`const val TYPE_COMMENT: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_CONTACT_FORM](-t-y-p-e_-c-o-n-t-a-c-t_-f-o-r-m.md) | A contact form or feedback form submission.`const val TYPE_CONTACT_FORM: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_FORUM_POST](-t-y-p-e_-f-o-r-u-m_-p-o-s-t.md) | A top-level forum post.`const val TYPE_FORUM_POST: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_MESSAGE](-t-y-p-e_-m-e-s-s-a-g-e.md) | A message sent between just a few users.`const val TYPE_MESSAGE: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_PINGBACK](-t-y-p-e_-p-i-n-g-b-a-c-k.md) | A pingback.`const val TYPE_PINGBACK: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_REPLY](-t-y-p-e_-r-e-p-l-y.md) | A reply to a top-level forum post.`const val TYPE_REPLY: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_SIGNUP](-t-y-p-e_-s-i-g-n-u-p.md) | A new user account.`const val TYPE_SIGNUP: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_TRACKBACK](-t-y-p-e_-t-r-a-c-k-b-a-c-k.md) | A trackback.`const val TYPE_TRACKBACK: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [TYPE_TWEET](-t-y-p-e_-t-w-e-e-t.md) | A Twitter message.`const val TYPE_TWEET: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
