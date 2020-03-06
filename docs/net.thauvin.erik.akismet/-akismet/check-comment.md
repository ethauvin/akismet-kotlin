[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](index.md) / [checkComment](./check-comment.md)

# checkComment

`@JvmOverloads fun checkComment(comment: `[`AkismetComment`](../-akismet-comment/index.md)`, trueOnError: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L275)

Comment Check.

This is the call you will make the most. It takes a number of arguments and characteristics about the submitted
content and then returns a thumbs up or thumbs down. Performance can drop dramatically if you choose to exclude
data points. The more data you send Akismet about each comment, the greater the accuracy. They recommend erring
on the side of including too much data

By default, if an error (IO, empty response from Akismet, etc.) occurs the function will return `false` and
log the error, use the `trueOnError` parameter to change this behavior.

See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.

### Parameters

`trueOnError` - Set to return `true` on error.

**Return**
`true` if the comment is spam, `false` if the comment is not.

