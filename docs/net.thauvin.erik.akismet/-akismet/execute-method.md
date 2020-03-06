[docs](../../index.md) / [net.thauvin.erik.akismet](../index.md) / [Akismet](index.md) / [executeMethod](./execute-method.md)

# executeMethod

`@JvmOverloads fun executeMethod(apiUrl: HttpUrl?, formBody: FormBody, trueOnError: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) [(source)](https://github.com/ethauvin/akismet-kotlin/tree/master/src/main/kotlin/net/thauvin/erik/akismet/Akismet.kt#L329)

Execute a call to an Akismet REST API method.

### Parameters

`apiUrl` - The Akismet API URL endpoint. (e.g. https://rest.akismet.com/1.1/verify-key)

`formBody` - The HTTP POST form body containing the request parameters to be submitted.

`trueOnError` - Set to return `true` on error (IO, empty response, etc.)