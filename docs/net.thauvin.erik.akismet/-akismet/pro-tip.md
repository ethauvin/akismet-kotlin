[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](index.md) / [proTip](./pro-tip.md)

# proTip

`var proTip: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L173)

The `x-akismet-pro-tip` header from the last operation, if any.

If the `x-akismet-pro-tip` header is set to discard, then Akismet has determined that the comment is blatant
spam, and you can safely discard it without saving it in any spam queue.

See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.

**See Also**

[Akismet.isDiscard](is-discard.md)

