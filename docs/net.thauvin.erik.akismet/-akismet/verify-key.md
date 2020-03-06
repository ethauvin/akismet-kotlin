[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](index.md) / [verifyKey](./verify-key.md)

# verifyKey

`fun verifyKey(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L249)

Key Verification.

Key verification authenticates your key before calling the [comment check](check-comment.md),
[submit spam](submit-spam.md), or [submit ham](submit-ham.md) methods. This is the first call that you
should make to Akismet and is especially useful if you will have multiple users with their own Akismet
subscriptions using your application.

See the [Akismet API](https://akismet.com/development/api/#verify-key) for more details.

**Return**
`true` if the key is valid, `false` otherwise.

**See Also**

[Akismet.isVerifiedKey](is-verified-key.md)

