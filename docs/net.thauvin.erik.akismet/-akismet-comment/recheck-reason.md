[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [AkismetComment](index.md) / [recheckReason](./recheck-reason.md)

# recheckReason

`var recheckReason: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/AkismetComment.kt#L215)

If you are sending content to Akismet to be rechecked, such as a post that has been edited or old pending
comments that you'd like to recheck, include this parameter with a string describing why the content is
being rechecked.

For example: `edit`

