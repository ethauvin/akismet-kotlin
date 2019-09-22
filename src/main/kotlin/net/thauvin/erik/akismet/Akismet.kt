/*
 * Akismet.kt
 *
 * Copyright (c) 2019, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
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

import net.thauvin.erik.semver.Version
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A small Kotlin/Java library for accessing the Akismet service.
 *
 * @constructor Create new instance using the provided [Akismet](https://www.askimet.com/) API key.
 */
@Version(properties = "version.properties", type = "kt")
open class Akismet(apiKey: String) {
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
            require(!value.isBlank()) { "A Blog URL must be specified." }
            field = value
        }

    /**
     * The application name to be used in the user agent string.
     *
     * See the [Akismet API](https://akismet.com/development/api/#detailed-docs) for more details.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var applicationName = ""

    /**
     * The application version to be used in the user agent string.
     *
     * See the [Akismet API](https://akismet.com/development/api/#detailed-docs) for more details.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var applicationVersion = ""

    /**
     * Check if the API Key has been verified.
     */
    var isVerifiedKey: Boolean = false
        private set

    /**
     * The [HTTP status code](https://www.restapitutorial.com/httpstatuscodes.html) of the last operation.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var httpStatusCode: Int = 0
        private set

    /**
     * The actual response sent by Akismet from the last operation.
     *
     * For example: _true_, _false_, _valid_, _invalid_, etc.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var response: String = ""
        private set

    /**
     * The _x-akismet-pro-tip_ header from the last operation, if any.
     *
     * If the _x-akismet-pro-tip_ header is set to discard, then Akismet has determined that the comment is blatant spam,
     * and you can safely discard it without saving it in any spam queue.
     *
     * Read more about this feature in this
     * [Akismet blog post](https://blog.akismet.com/2014/04/23/theres-a-ninja-in-your-akismet/).
     *
     * @see [Akismet.isDiscard]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var proTip: String = ""
        private set

    /**
     * Set to true if Akismet has determined that the last [checked comment][checkComment] is blatant spam, and you
     * can safely discard it without saving it in any spam queue.
     *
     * Read more about this feature in this
     * [Akismet blog post](https://blog.akismet.com/2014/04/23/theres-a-ninja-in-your-akismet/).
     *
     * @see [Akismet.proTip]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var isDiscard: Boolean = false
        private set

    /**
     * The _x-akismet-debug-help_ header from the last operation, if any.
     *
     * If the call returns neither _true_ nor _false_, the _x-akismet-debug-help_ header will provide context for any
     * error that has occurred.
     *
     * Note that the _x-akismet-debug-help_ header will not always be sent if a response does not return _false_
     * or _true_.
     *
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     */
    var debugHelp: String = ""
        private set

    /**
     * The logger instance.
     */
    val logger: Logger by lazy { Logger.getLogger(Akismet::class.java.simpleName) }

    init {
        require(
            (apiKey.isNotBlank() &&
                apiKey.length == 12 &&
                apiKey.matches(Regex("[A-Za-z0-9]+")))
        ) {
            "An Akismet API key must be specified."
        }

        this.apiKey = apiKey

        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, message.replace(apiKey, "xxxxxxxx" + apiKey.substring(8), true))
                }
            }
        })
        logging.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder().addInterceptor(logging).build()
    }

    /**
     * Create a new instance using an [Akismet](https://www.askimet.com/) API key and URL registered with Akismet.
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
     * @return _true_ if the key is valid, _false_ otherwise.
     * @see [isVerifiedKey]
     */
    fun verifyKey(): Boolean {
        val body = FormBody.Builder().apply {
            add("key", apiKey)
            add("blog", blog)
        }.build()
        isVerifiedKey = executeMethod(buildApiUrl(verifyMethod), body)
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
     * By default, if an error (IO, empty response from Akismet, etc.) occurs the function will return _false_ and
     * log the error, use the _trueOnError_ parameter to change this behavior.
     *
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     *
     * @param trueOnError Set to return _true_ on error.
     * @return _true_ if the comment is spam, _false_ if the comment is ham.
     */
    @JvmOverloads
    fun checkComment(comment: AkismetComment, trueOnError: Boolean = false): Boolean {
        return executeMethod(buildApiUrl("comment-check"), buildFormBody(comment), trueOnError)
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
     * @return _true_ if the comment was submitted, _false_ otherwise.
     */
    fun submitSpam(comment: AkismetComment): Boolean {
        return executeMethod(buildApiUrl("submit-spam"), buildFormBody(comment))
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
     * @return _true_ if the comment was submitted, _false_ otherwise.
     */
    fun submitHam(comment: AkismetComment): Boolean {
        return executeMethod(buildApiUrl("submit-ham"), buildFormBody(comment))
    }

    /**
     * Convert a date to a UTC timestamp. (ISO 8601)
     *
     * @see [AkismetComment.dateGmt]
     * @see [AkismetComment.postModifiedGmt]
     */
    fun dateToGmt(date: Date): String {
        return DateTimeFormatter.ISO_DATE_TIME.format(
            OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS)
        )
    }

    /**
     * Convert a locale date/time to a UTC timestamp. (ISO 8601)
     *
     * @see [AkismetComment.dateGmt]
     * @see [AkismetComment.postModifiedGmt]
     */
    fun dateToGmt(date: LocalDateTime): String {
        return DateTimeFormatter.ISO_DATE_TIME.format(
            date.atOffset(OffsetDateTime.now().offset).truncatedTo(ChronoUnit.SECONDS)
        )
    }

    /**
     * Execute a call to an Akismet REST API method.
     *
     * @param apiUrl The Akismet API URL endpoint. (e.g. https://rest.akismet.com/1.1/verify-key)
     * @param formBody The HTTP POST form body containing the request parameters to be submitted.
     * @param trueOnError Set to return _true_ on error (IO, empty response, etc.)
     */
    @JvmOverloads
    fun executeMethod(apiUrl: HttpUrl?, formBody: FormBody, trueOnError: Boolean = false): Boolean {
        reset()
        if (apiUrl != null) {
            val request = Request.Builder().url(apiUrl).post(formBody).header("User-Agent", buildUserAgent()).build()
            try {
                val result = client.newCall(request).execute()
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
                        logger.warning("Unexpected response: $body")
                        return trueOnError
                    }
                } else {
                    val message = "An empty response was received from Akismet."
                    if (debugHelp.isNotBlank()) {
                        logger.warning("$message: $debugHelp")
                    } else {
                        logger.warning(message)
                    }
                    return trueOnError
                }
            } catch (e: IOException) {
                logger.log(Level.SEVERE, "An IO error occurred while communicating with the Akismet service.", e)
                return trueOnError
            }
        } else {
            logger.severe("Invalid API end point URL.")
            return trueOnError
        }
        return false
    }

    /**
     * Reset the [debugHelp], [httpStatusCode], [isDiscard], [isVerifiedKey], [proTip], and [response] properties.
     */
    fun reset() {
        debugHelp = ""
        httpStatusCode = 0
        isDiscard = false
        isVerifiedKey = false
        proTip = ""
        response = ""
    }

    private fun buildApiUrl(method: String): HttpUrl? {
        if (method == verifyMethod) {
            return String.format(apiEndPoint, "", method).toHttpUrlOrNull()
        }
        return String.format(apiEndPoint, "$apiKey.", method).toHttpUrlOrNull()
    }

    private fun buildFormBody(comment: AkismetComment): FormBody {
        require(!(comment.userIp.isBlank() && comment.userAgent.isBlank())) { "userIp and/or userAgent are required." }
        return FormBody.Builder().apply {
            add("blog", blog)

            with(comment) {
                add("user_ip", userIp)
                add("user_agent", userAgent)

                if (referrer.isNotBlank()) {
                    add("referrer", referrer)
                }
                if (permalink.isNotBlank()) {
                    add("permalink", permalink)
                }
                if (type.isNotBlank()) {
                    add("comment_type", type)
                }
                if (author.isNotBlank()) {
                    add("comment_author", author)
                }
                if (authorEmail.isNotBlank()) {
                    add("comment_author_email", authorEmail)
                }
                if (authorUrl.isNotBlank()) {
                    add("comment_author_url", authorUrl)
                }
                if (content.isNotBlank()) {
                    add("comment_content", content)
                }
                if (dateGmt.isNotBlank()) {
                    add("comment_date_gmt", dateGmt)
                }
                if (postModifiedGmt.isNotBlank()) {
                    add("comment_post_modified_gmt", postModifiedGmt)
                }
                if (blogLang.isNotBlank()) {
                    add("blog_lang", blogLang)
                }
                if (blogCharset.isNotBlank()) {
                    add("blog_charset", blogCharset)
                }
                if (userRole.isNotBlank()) {
                    add("user_role", userRole)
                }
                if (isTest) {
                    add("is_test", "1")
                }
                if (recheckReason.isNotBlank()) {
                    add("recheck_reason", recheckReason)
                }

                serverEnv.forEach { (k, v) -> add(k, v) }
            }
        }.build()
    }

    internal fun buildUserAgent(): String {
        return if (applicationName.isNotBlank() && applicationVersion.isNotBlank()) {
            "$applicationName/$applicationVersion | $libUserAgent"
        } else {
            libUserAgent
        }
    }
}
