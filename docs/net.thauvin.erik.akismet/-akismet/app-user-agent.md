[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](index.md) / [appUserAgent](./app-user-agent.md)

# appUserAgent

`var appUserAgent: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L126)

The application user agent to be sent to Akismet.

If possible, the application user agent string should always use the following format:

```
    Application Name/Version
```

The library's own user agent string will automatically be appended.

See the [Akismet API](https://akismet.com/development/api/#detailed-docs) for more details.

