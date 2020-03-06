[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](index.md) / [debugHelp](./debug-help.md)

# debugHelp

`var debugHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L199)

The `x-akismet-debug-help` header from the last operation, if any.

If the call returns neither `true` nor `false`, the `x-akismet-debug-help` header will provide context for any
error that has occurred.

Note that the `x-akismet-debug-help` header will not always be sent if a response does not return `false`
or `true`.

See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.

