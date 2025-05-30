/*
 * Akismet.kt
 *
 * Copyright 2019-2025 Erik C. Thauvin (erik@thauvin.net)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.thauvin.erik.akismet

import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Provides access to the [Akismet API](https://akismet.com/development/api/).
 *
 * @constructor Creates a new instance using the provided [Akismet](https://www.askimet.com/) API key.
 */
open class Akismet(apiKey: String) {
    companion object {
        /**
         * The logger instance.
         */
        @JvmStatic
        val logger: Logger by lazy { Logger.getLogger(Akismet::class.java.name) }

        /**
         * (Re)Creates a [comment][AkismetComment] from a JSON string.
         *
         * @see [AkismetComment.toString]
         */
        @JvmStatic
        fun jsonComment(json: String): AkismetComment {
            return Json.decodeFromString<AkismetComment>(json)
        }

        /**
         * Converts a date to a UTC timestamp. (ISO 8601)
         *
         * @see [AkismetComment.dateGmt]
         * @see [AkismetComment.postModifiedGmt]
         */
        @JvmStatic
        fun dateToGmt(date: Date): String {
            return DateTimeFormatter.ISO_DATE_TIME.format(
                OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS)
            )
        }

        /**
         * Converts a locale date/time to a UTC timestamp. (ISO 8601)
         *
         * @see [AkismetComment.dateGmt]
         * @see [AkismetComment.postModifiedGmt]
         */
        @JvmStatic
        fun dateToGmt(date: LocalDateTime): String {
            return DateTimeFormatter.ISO_DATE_TIME.format(
                date.atOffset(OffsetDateTime.now().offset).truncatedTo(ChronoUnit.SECONDS)
            )
        }
    }

    private val apiEndPoint = "https://%srest.akismet.com/1.1/%s"
    private val libUserAgent = "${GeneratedVersion.PROJECT}/${GeneratedVersion.VERSION}"
    private val verifyMethod = "verify-key"
    private var apiKey: String
    private var client: OkHttpClient

    /**
     * The URL registered with Akismet.
     */
    var blog = ""
        set(value) {
            require(value.isNotBlank()) { "A Blog URL must be specified." }
            field = value
        }

    /**
     * The application user agent to be sent to Akismet.
     *
     * If possible, the application user agent string should always use the following format:
     *
     * ```
     *     Application Name/Version
     * ```
     *
     * The library's own user agent string will automatically be appended.
     *
     * See the [Akismet API](https://akismet.com/development/api/#detailed-docs) for more details.
     */
    var appUserAgent = ""

    /**
     * Set to `true` if the API Key has been verified.
     *
     * @see [Akismet.verifyKey]
     */
    var isVerifiedKey: Boolean = false
        private set

    /**
     * The [HTTP status code](https://www.restapitutorial.com/httpstatuscodes.html) of the last operation.
     */
    var httpStatusCode: Int = 0
        private set

    /**
     * The actual response sent by Akismet from the last operation.
     *
     * For example: `true`, `false`, `valid`, `invalid`, etc.
     */
    var response: String = ""
        private set

    /**
     * The error message.
     *
     * The error (IO, empty response from Akismet, etc.) message is also logged as a warning.
     *
     * @see [Akismet.checkComment]
     */
    var errorMessage: String = ""
        private set

    /**
     * The `x-akismet-pro-tip` header from the last operation, if any.
     *
     * If the `x-akismet-pro-tip` header is set to discard, then Akismet has determined that the comment is blatant
     * spam, and you can safely discard it without saving it in any spam queue.
     *
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     *
     * @see [Akismet.isDiscard]
     */
    var proTip: String = ""
        private set

    /**
     * Set to `true` if Akismet has determined that the last [checked comment][checkComment] is blatant spam, and you
     * can safely discard it without saving it in any spam queue.
     *
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     *
     * @see [Akismet.proTip]
     */
    var isDiscard: Boolean = false
        private set

    /**
     * The `x-akismet-debug-help` header from the last operation, if any.
     *
     * If the call returns neither `true` nor `false`, the `x-akismet-debug-help` header will provide context for any
     * error that has occurred.
     *
     * Note that the `x-akismet-debug-help` header will not always be sent if a response does not return `false`
     * or `true`.
     *
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     */
    var debugHelp: String = ""
        private set


    init {
        require(
            (apiKey.isNotBlank() &&
                    apiKey.length == 12 &&
                    apiKey.matches(Regex("[A-Za-z0-9\\-]+")))
        ) {
            "An Akismet API key must be specified."
        }

        this.apiKey = apiKey

        val logging = HttpLoggingInterceptor { message ->
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, message.replace(apiKey, "xxxxxxxx" + apiKey.substring(8), true))
            }
        }
        logging.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder().addInterceptor(logging).build()
    }

    /**
     * Creates a new instance using an [Akismet](https://www.askimet.com/) API key and URL registered with Akismet.
     */
    constructor(apiKey: String, blog: String) : this(apiKey) {
        this.blog = blog
    }

    /**
     * Key Verification.
     *
     * Key verification authenticates your key before calling the [comment check][Akismet.checkComment],
     * [submit spam][Akismet.submitSpam], or [submit ham][Akismet.submitHam] methods. This is the first call that you
     * should make to Akismet and is especially useful if you will have multiple users with their own Akismet
     * subscriptions using your application.
     *
     * See the [Akismet API](https://akismet.com/development/api/#verify-key) for more details.
     *
     * @return `true` if the key is valid, `false` otherwise
     * @see [Akismet.isVerifiedKey]
     */
    fun verifyKey(): Boolean {
        val body = FormBody.Builder().apply {
            add("key", apiKey)
            add("blog", blog)
        }.build()
        isVerifiedKey = executeMethod(verifyMethod.toApiUrl(), body)
        return isVerifiedKey
    }

    /**
     * Comment Check.
     *
     * This is the call you will make the most. It takes a number of arguments and characteristics about the submitted
     * content and then returns a thumbs up or thumbs down. Performance can drop dramatically if you choose to exclude
     * data points. The more data you send Akismet about each comment, the greater the accuracy. They recommend erring
     * on the side of including too much data
     *
     * By default, if an error (IO, empty response from Akismet, etc.) occurs the function will return `false` and
     * log the error, use the `trueOnError` parameter to change this behavior.
     *
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     *
     * @param trueOnError Set to return `true` on error
     * @return `true` if the comment is spam, `false` if the comment is not
     */
    @JvmOverloads
    fun checkComment(comment: AkismetComment, trueOnError: Boolean = false): Boolean {
        return executeMethod("comment-check".toApiUrl(), buildFormBody(comment), trueOnError)
    }

    /**
     * Submit Spam. (Missed Spam)
     *
     * This call is for submitting comments that weren't marked as spam but should have been.
     *
     * It is very important that the values you submit with this call match those of your
     * [comment check][Akismet.checkComment] calls as closely as possible. In order to learn from its mistakes,
     * Akismet needs to match your missed spam and false positive reports to the original comment-check API calls made
     * when the content was first posted. While it is normal for less information to be available for submit-spam and
     * submit-ham calls (most comment systems and forums will not store all metadata), you should ensure that the
     * values that you do send match those of the original content.
     *
     * See the [Akismet API](https://akismet.com/development/api/#submit-spam) for more details.
     *
     * @return `true` if the comment was submitted, `false` otherwise
     */
    fun submitSpam(comment: AkismetComment): Boolean {
        return executeMethod("submit-spam".toApiUrl(), buildFormBody(comment))
    }

    /**
     * Submit Ham. (False Positives)
     *
     * This call is intended for the submission of false positives - items that were incorrectly classified as spam by
     * Akismet. It takes identical arguments as [comment check][Akismet.checkComment] and
     * [submit spam][Akismet.submitSpam].
     *
     * It is very important that the values you submit with this call match those of your
     * [comment check][Akismet.checkComment] calls as closely as possible. In order to learn from its mistakes,
     * Akismet needs to match your missed spam and false positive reports to the original comment-check API calls made
     * when the content was first posted. While it is normal for less information to be available for submit-spam and
     * submit-ham calls (most comment systems and forums will not store all metadata), you should ensure that the
     * values that you do send match those of the original content.
     *
     * See the [Akismet API](https://akismet.com/development/api/#submit-ham) for more details.
     *
     * @return `true` if the comment was submitted, `false` otherwise
     */
    fun submitHam(comment: AkismetComment): Boolean {
        return executeMethod("submit-ham".toApiUrl(), buildFormBody(comment))
    }

    /**
     * Executes a call to an Akismet REST API method.
     *
     * @param apiUrl The Akismet API URL endpoint. (e.g., https://rest.akismet.com/1.1/verify-key)
     * @param formBody The HTTP POST form body containing the request parameters to be submitted
     * @param trueOnError Set to return `true` on error (IO, empty response, etc.)
     */
    @JvmOverloads
    fun executeMethod(apiUrl: HttpUrl, formBody: FormBody, trueOnError: Boolean = false): Boolean {
        reset()
        val request = if (formBody.size == 0) {
            Request.Builder().url(apiUrl).header("User-Agent", buildUserAgent()).build()
        } else {
            Request.Builder().url(apiUrl).post(formBody).header("User-Agent", buildUserAgent()).build()
        }
        try {
            client.newCall(request).execute().use { result ->
                httpStatusCode = result.code
                proTip = result.header("x-akismet-pro-tip", "").toString().trim()
                isDiscard = (proTip == "discard")
                debugHelp = result.header("x-akismet-debug-help", "").toString().trim()
                val body = result.body?.string()
                if (body != null) {
                    response = body.trim()
                    if (response == "valid" || response == "true" || response.startsWith("Thanks")) {
                        return true
                    } else if (response != "false" && response != "invalid") {
                        errorMessage = "Unexpected response: " + body.ifBlank { "<blank>" }
                    }
                } else {
                    val message = "No response body was received from Akismet."
                    errorMessage = if (debugHelp.isNotBlank()) {
                        "$message: $debugHelp"
                    } else {
                        message
                    }
                }
            }
        } catch (e: IOException) {
            errorMessage = "An IO error occurred while communicating with the Akismet service: ${e.message}"
        }

        if (errorMessage.isNotEmpty()) {
            if (logger.isLoggable(Level.WARNING)) logger.warning(errorMessage)
            return trueOnError
        }

        return false
    }

    /**
     * Resets the [debugHelp], [errorMessage], [httpStatusCode], [isDiscard], [isVerifiedKey], [proTip], and
     * [response] properties.
     */
    fun reset() {
        debugHelp = ""
        errorMessage = ""
        httpStatusCode = 0
        isDiscard = false
        isVerifiedKey = false
        proTip = ""
        response = ""
    }

    private fun String.toApiUrl(): HttpUrl {
        return if (this == verifyMethod) {
            String.format(apiEndPoint, "", this).toHttpUrl()
        } else {
            String.format(apiEndPoint, "$apiKey.", this).toHttpUrl()
        }
    }

    private fun buildFormBody(comment: AkismetComment): FormBody {
        require(!(comment.userIp.isBlank() && comment.userAgent.isBlank())) { "userIp and/or userAgent are required." }
        return FormBody.Builder().apply {
            add("blog", blog)

            with(comment) {
                add("user_ip", userIp)
                add("user_agent", userAgent)

                if (!referrer.isNullOrBlank()) {
                    add("referrer", referrer.toString())
                }
                if (!permalink.isNullOrBlank()) {
                    add("permalink", permalink.toString())
                }
                if (type != CommentType.NONE) {
                    add("comment_type", type.value)
                }
                if (!author.isNullOrBlank()) {
                    add("comment_author", author.toString())
                }
                if (!authorEmail.isNullOrBlank()) {
                    add("comment_author_email", authorEmail.toString())
                }
                if (!authorUrl.isNullOrBlank()) {
                    add("comment_author_url", authorUrl.toString())
                }
                if (!content.isNullOrBlank()) {
                    add("comment_content", content.toString())
                }
                if (!dateGmt.isNullOrBlank()) {
                    add("comment_date_gmt", dateGmt.toString())
                }
                if (!postModifiedGmt.isNullOrBlank()) {
                    add("comment_post_modified_gmt", postModifiedGmt.toString())
                }
                if (!blogLang.isNullOrBlank()) {
                    add("blog_lang", blogLang.toString())
                }
                if (!blogCharset.isNullOrBlank()) {
                    add("blog_charset", blogCharset.toString())
                }
                if (!userRole.isNullOrBlank()) {
                    add("user_role", userRole.toString())
                }
                if (isTest) {
                    add("is_test", "1")
                }
                if (!recheckReason.isNullOrBlank()) {
                    add("recheck_reason", recheckReason.toString())
                }

                serverEnv.forEach { (k, v) -> add(k, v) }
            }
        }.build()
    }

    internal fun buildUserAgent(): String {
        return if (appUserAgent.isNotBlank()) {
            "$appUserAgent | $libUserAgent"
        } else {
            libUserAgent
        }
    }
}
