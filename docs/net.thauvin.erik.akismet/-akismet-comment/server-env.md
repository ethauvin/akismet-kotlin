[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [AkismetComment](index.md) / [serverEnv](./server-env.md)

# serverEnv

`var serverEnv: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/AkismetComment.kt#L228)

In PHP, there is an array of environmental variables called `$_SERVER` that contains information about the Web
server itself as well as a key/value for every HTTP header sent with the request. This data is highly useful to
Akismet.

How the submitted content interacts with the server can be very telling, so please include as much of it as
possible.

