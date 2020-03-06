[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](./index.md)

# Akismet

`open class Akismet` [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L59)

Provides access to the [Akismet API](https://akismet.com/development/api/).

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | Create a new instance using an [Akismet](https://www.askimet.com/) API key and URL registered with Akismet.`Akismet(apiKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, blog: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`<br>Creates new instance using the provided [Akismet](https://www.askimet.com/) API key.`Akismet(apiKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)` |

### Properties

| Name | Summary |
|---|---|
| [appUserAgent](app-user-agent.md) | The application user agent to be sent to Akismet.`var appUserAgent: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [blog](blog.md) | The URL registered with Akismet.`var blog: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [debugHelp](debug-help.md) | The `x-akismet-debug-help` header from the last operation, if any.`var debugHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [errorMessage](error-message.md) | The error message.`var errorMessage: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [httpStatusCode](http-status-code.md) | The [HTTP status code](https://www.restapitutorial.com/httpstatuscodes.html) of the last operation.`var httpStatusCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [isDiscard](is-discard.md) | Set to true if Akismet has determined that the last [checked comment](check-comment.md) is blatant spam, and you can safely discard it without saving it in any spam queue.`var isDiscard: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [isVerifiedKey](is-verified-key.md) | Check if the API Key has been verified`var isVerifiedKey: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [logger](logger.md) | The logger instance.`val logger: `[`Logger`](https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html) |
| [proTip](pro-tip.md) | The `x-akismet-pro-tip` header from the last operation, if any.`var proTip: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [response](response.md) | The actual response sent by Akismet from the last operation.`var response: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Functions

| Name | Summary |
|---|---|
| [checkComment](check-comment.md) | Comment Check.`fun checkComment(comment: `[`AkismetComment`](../-akismet-comment/index.md)`, trueOnError: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [executeMethod](execute-method.md) | Execute a call to an Akismet REST API method.`fun executeMethod(apiUrl: HttpUrl?, formBody: FormBody, trueOnError: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [reset](reset.md) | Reset the [debugHelp](debug-help.md), [errorMessage](error-message.md), [httpStatusCode](http-status-code.md), [isDiscard](is-discard.md), [isVerifiedKey](is-verified-key.md), [proTip](pro-tip.md), and [response](response.md) properties.`fun reset(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [submitHam](submit-ham.md) | Submit Ham. (False Positives)`fun submitHam(comment: `[`AkismetComment`](../-akismet-comment/index.md)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [submitSpam](submit-spam.md) | Submit Spam. (Missed Spam)`fun submitSpam(comment: `[`AkismetComment`](../-akismet-comment/index.md)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [verifyKey](verify-key.md) | Key Verification.`fun verifyKey(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [dateToGmt](date-to-gmt.md) | Convert a date to a UTC timestamp. (ISO 8601)`fun dateToGmt(date: `[`Date`](https://docs.oracle.com/javase/8/docs/api/java/util/Date.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Convert a locale date/time to a UTC timestamp. (ISO 8601)`fun dateToGmt(date: `[`LocalDateTime`](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [jsonComment](json-comment.md) | (Re)Create a [comment](../-akismet-comment/index.md) from a JSON string.`fun jsonComment(json: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`AkismetComment`](../-akismet-comment/index.md) |
