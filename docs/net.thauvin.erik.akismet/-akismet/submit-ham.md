[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](index.md) / [submitHam](./submit-ham.md)

# submitHam

`fun submitHam(comment: `[`AkismetComment`](../-akismet-comment/index.md)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L317)

Submit Ham. (False Positives)

This call is intended for the submission of false positives - items that were incorrectly classified as spam by
Akismet. It takes identical arguments as [comment check](check-comment.md) and
[submit spam](submit-spam.md).

It is very important that the values you submit with this call match those of your
[comment check](check-comment.md) calls as closely as possible. In order to learn from its mistakes,
Akismet needs to match your missed spam and false positive reports to the original comment-check API calls made
when the content was first posted. While it is normal for less information to be available for submit-spam and
submit-ham calls (most comment systems and forums will not store all metadata), you should ensure that the
values that you do send match those of the original content.

See the [Akismet API](https://akismet.com/development/api/#submit-ham) for more details.

**Return**
`true` if the comment was submitted, `false` otherwise.

