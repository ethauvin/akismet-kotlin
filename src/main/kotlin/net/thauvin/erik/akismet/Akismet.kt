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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest

/**
 * A small Kotlin/Java library for accessing the Akismet service.
 *
 * @constructor Creates a new instance.
 *
 * @param apiKey The [Akismet](https://www.askimet.com/) API key.
 * @param blog The URL registered with Akismet.
 *
 * @author [Erik C. Thauvin](https://erik.thauvin.net/)
 */
@Version(properties = "version.properties", type = "kt")
open class Akismet(apiKey: String, blog: String) {
    @Suppress("unused")
    companion object {
        /** A blog comment. */
        const val COMMENT_TYPE_COMMENT = "comment"
        /** A top-level forum post. */
        const val COMMENT_TYPE_FORUM_POST = "forum-post"
        /** A reply to a top-level forum post. */
        const val COMMENT_TYPE_REPLY = "reply"
        /** A blog post. */
        const val COMMENT_TYPE_BLOG_POST = "blog-post"
        /** A contact form or feedback form submission. */
        const val COMMENT_TYPE_CONTACT_FORM = "contact-form"
        /**  A new user account. */
        const val COMMENT_TYPE_SIGNUP = "signup"
        /**  A message sent between just a few users. */
        const val COMMENT_TYPE_MESSAGE = "message"
        /** Administrator role */
        const val ADMIN_ROLE = "administrator"
    }

    private val apiEndPoint = "https://%s.akismet.com/1.1/%s"
    private val libUserAgent = "${GeneratedVersion.PROJECT}/${GeneratedVersion.VERSION}"
    private val verifyMethod = "verify-key"
    private var apiKey: String
    private var blog: String
    private var client: OkHttpClient

    var isValidKey: Boolean = false
    /**
     * Check if the API Key has been verified.
     */
    var isVerifiedKey: Boolean = false
        private set

    /**
     * The HTTP status code of the last operation.
     */
        private set
    /**
     * The X-akismet-pro-tip header from the last operation, if any.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var proTip: String = ""
        private set
    /**
     * The X-akismet-error header from the last operation, if any.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var error: String = ""
        private set
    /**
     * The X-akismet-debug-help header from the last operation, if any.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var degugHelp: String = ""
        private set

    /**
     * The logger instance.
     */
    val logger: Logger by lazy { Logger.getLogger(Akismet::class.java.simpleName) }

    init {
        require(!apiKey.isBlank() || apiKey.length != 12) { "An Akismet API key must be specified." }
        require(!blog.isBlank()) { "A Blog URL must be specified." }

        this.apiKey = apiKey
        this.blog = blog

        if (logger.isLoggable(Level.FINE)) {
            val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    logger.log(Level.FINE, message)
                }
            })
            logging.level = HttpLoggingInterceptor.Level.BODY
            client = OkHttpClient.Builder().addInterceptor(logging).build()
        } else {
            client = OkHttpClient()
        }
    }

    /**
     * Key Verification.
     * See the [Akismet API](https://akismet.com/development/api/#verify-key) for more details.
     */
    fun verifyKey(): Boolean {
        val params = HashMap<String, String>()
        params["key"] = apiKey
        params["blog"] = blog
        isValidKey = executeMethod(verifyMethod, FormBody.Builder().build())
        return isValidKey
    }

    /**
     * Comment Check using the content of a
     * [HttpServletRequest](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html).
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     */
    @JvmOverloads
    fun checkComment(
        request: HttpServletRequest,
        permalink: String = "",
        type: String = "",
        author: String = "",
        authorEmail: String = "",
        authorUrl: String = "",
        content: String = "",
        dateGmt: String = "",
        postModifiedGmt: String = "",
        blogLang: String = "",
        blogCharset: String = "",
        userRole: String = "",
        isTest: Boolean = false,
        recheckReason: String = "",
        other: Map<String, String> = emptyMap()
    ): Boolean {
        return checkComment(
            userIp = request.remoteAddr,
            userAgent = request.getHeader("User-Agent"),
            referrer = request.getHeader("Referer"),
            permalink = permalink,
            type = type,
            author = author,
            authorEmail = authorEmail,
            authorUrl = authorUrl,
            content = content,
            dateGmt = dateGmt,
            postModifiedGmt = postModifiedGmt,
            blogLang = blogLang,
            blogCharset = blogCharset,
            userRole = userRole,
            isTest = isTest,
            recheckReason = recheckReason,
            other = buildPhpVars(request, other))
    }

    /**
     * Comment Check. See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     */
    @JvmOverloads
    fun checkComment(
        userIp: String,
        userAgent: String,
        referrer: String = "",
        permalink: String = "",
        type: String = "",
        author: String = "",
        authorEmail: String = "",
        authorUrl: String = "",
        content: String = "",
        dateGmt: String = "",
        postModifiedGmt: String = "",
        blogLang: String = "",
        blogCharset: String = "",
        userRole: String = "",
        isTest: Boolean = false,
        recheckReason: String = "",
        other: Map<String, String> = emptyMap()
    ): Boolean {

        require(!(userIp.isBlank() && userAgent.isBlank())) { "userIp and/or userAgent are required." }

        return executeMethod(
            "comment-check",
            buildFormBody(
                userIp = userIp,
                userAgent = userAgent,
                referrer = referrer,
                permalink = permalink,
                type = type,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content,
                dateGmt = dateGmt,
                postModifiedGmt = postModifiedGmt,
                blogLang = blogLang,
                blogCharset = blogCharset,
                userRole = userRole,
                isTest = isTest,
                recheckReason = recheckReason,
                other = other))
    }

    /**
     * Submit Spam (missed spam) using the content of a
     * [HttpServletRequest](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html).
     * See the [Akismet API](https://akismet.com/development/api/#submit-spam) for more details.
     */
    @JvmOverloads
    fun submitSpam(
        request: HttpServletRequest,
        permalink: String = "",
        type: String = "",
        author: String = "",
        authorEmail: String = "",
        authorUrl: String = "",
        content: String = "",
        dateGmt: String = "",
        postModifiedGmt: String = "",
        blogLang: String = "",
        blogCharset: String = "",
        userRole: String = "",
        isTest: Boolean = false,
        recheckReason: String = "",
        other: Map<String, String> = emptyMap()
    ): Boolean {
        return submitSpam(
            userIp = request.remoteAddr,
            userAgent = request.getHeader("User-Agent"),
            referrer = request.getHeader("Referer"),
            permalink = permalink,
            type = type,
            author = author,
            authorEmail = authorEmail,
            authorUrl = authorUrl,
            content = content,
            dateGmt = dateGmt,
            postModifiedGmt = postModifiedGmt,
            blogLang = blogLang,
            blogCharset = blogCharset,
            userRole = userRole,
            isTest = isTest,
            recheckReason = recheckReason,
            other = buildPhpVars(request, other))
    }

    /**
     * Submit Spam (missed spam).
     * See the [Akismet API](https://akismet.com/development/api/#submit-spam) for more details.
     */
    @JvmOverloads
    fun submitSpam(
        userIp: String,
        userAgent: String,
        referrer: String = "",
        permalink: String = "",
        type: String = "",
        author: String = "",
        authorEmail: String = "",
        authorUrl: String = "",
        content: String = "",
        dateGmt: String = "",
        postModifiedGmt: String = "",
        blogLang: String = "",
        blogCharset: String = "",
        userRole: String = "",
        isTest: Boolean = false,
        recheckReason: String = "",
        other: Map<String, String> = emptyMap()
    ): Boolean {
        return executeMethod(
            "submit-spam",
            buildFormBody(
                userIp = userIp,
                userAgent = userAgent,
                referrer = referrer,
                permalink = permalink,
                type = type,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content,
                dateGmt = dateGmt,
                postModifiedGmt = postModifiedGmt,
                blogLang = blogLang,
                blogCharset = blogCharset,
                userRole = userRole,
                isTest = isTest,
                recheckReason = recheckReason,
                other = other))
    }

    /**
     * Submit Ham (false positives) using the content of a
     * [HttpServletRequest](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html).
     * See the [Akismet API](https://akismet.com/development/api/#submit-ham) for more details.
     */
    @JvmOverloads
    fun submitHam(
        request: HttpServletRequest,
        permalink: String = "",
        type: String = "",
        author: String = "",
        authorEmail: String = "",
        authorUrl: String = "",
        content: String = "",
        dateGmt: String = "",
        postModifiedGmt: String = "",
        blogLang: String = "",
        blogCharset: String = "",
        userRole: String = "",
        isTest: Boolean = false,
        recheckReason: String = "",
        other: Map<String, String> = emptyMap()
    ): Boolean {
        return submitHam(
            userIp = request.remoteAddr,
            userAgent = request.getHeader("User-Agent"),
            referrer = request.getHeader("Referer"),
            permalink = permalink,
            type = type,
            author = author,
            authorEmail = authorEmail,
            authorUrl = authorUrl,
            content = content,
            dateGmt = dateGmt,
            postModifiedGmt = postModifiedGmt,
            blogLang = blogLang,
            blogCharset = blogCharset,
            userRole = userRole,
            isTest = isTest,
            recheckReason = recheckReason,
            other = buildPhpVars(request, other))
    }

    /**
     * Submit Ham.
     * See the [Akismet API](https://akismet.com/development/api/#submit-ham) for more details.
     */
    @JvmOverloads
    fun submitHam(
        userIp: String,
        userAgent: String,
        referrer: String = "",
        permalink: String = "",
        type: String = "",
        author: String = "",
        authorEmail: String = "",
        authorUrl: String = "",
        content: String = "",
        dateGmt: String = "",
        postModifiedGmt: String = "",
        blogLang: String = "",
        blogCharset: String = "",
        userRole: String = "",
        isTest: Boolean = false,
        recheckReason: String = "",
        other: Map<String, String> = emptyMap()
    ): Boolean {
        return executeMethod(
            "submit-ham",
            buildFormBody(
                userIp = userIp,
                userAgent = userAgent,
                referrer = referrer,
                permalink = permalink,
                type = type,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content,
                dateGmt = dateGmt,
                postModifiedGmt = postModifiedGmt,
                blogLang = blogLang,
                blogCharset = blogCharset,
                userRole = userRole,
                isTest = isTest,
                recheckReason = recheckReason,
                other = other))
    }

    private fun executeMethod(method: String, formBody: FormBody): Boolean {
        val apiUrl = buildApiUrl(method).toHttpUrlOrNull()
        if (apiUrl != null) {
            val request = Request.Builder().url(apiUrl).post(formBody).header("User-Agent", libUserAgent).build()
            try {
                val result = client.newCall(request).execute()
                proTip = result.header("x-akismet-pro-tip", "").toString()
                error = result.header("x-akismet-error", "").toString()
                degugHelp = result.header("X-akismet-debug-help", "").toString()
                val body = result.body?.string()
                if (body != null) {
                    val response = body.trim()
                    if (response.equals("valid", true) ||
                        response.equals("true", true) ||
                        response.startsWith("Thanks", true)) {
                        return true
                    }
                }
            } catch (e: IOException) {
                logger.log(Level.SEVERE, "An IO error occurred while communicating with the Akismet service.", e)
            }
        } else {
            logger.severe("Invalid API end point URL: $method. The API Key is likely invalid.")
        }
        return false
    }

    private fun buildApiUrl(method: String): String {
        if (method == verifyMethod) {
            return String.format(apiEndPoint, "rest", method)
        }
        return String.format(apiEndPoint, apiKey, method)
    }

    private fun buildPhpVars(request: HttpServletRequest, other: Map<String, String>): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["REMOTE_ADDR"] = request.remoteAddr
        params["REQUEST_URI"] = request.requestURI

        val names = request.headerNames
        while (names.hasMoreElements()) {
            val name = names.nextElement()
            if (!name.equals("cookie", true)) {
                params["HTTP_${name.toUpperCase()}"] = request.getHeader(name)
            }
        }

        if (other.isEmpty()) {
            params.putAll(other)
        }

        return params
    }

    private fun buildFormBody(
        userIp: String,
        userAgent: String,
        referrer: String,
        permalink: String,
        type: String,
        author: String,
        authorEmail: String,
        authorUrl: String,
        content: String,
        dateGmt: String,
        postModifiedGmt: String,
        blogLang: String,
        blogCharset: String,
        userRole: String,
        isTest: Boolean,
        recheckReason: String,
        other: Map<String, String>
    ): FormBody {
        return FormBody.Builder().apply {
            add("blog", blog)
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
                add("is_test", "true")
            }
            if (recheckReason.isNotBlank()) {
                add("recheck_reason", recheckReason)
            }

            other.forEach { (k, v) -> add(k, v) }
        }.build()
    }
}
