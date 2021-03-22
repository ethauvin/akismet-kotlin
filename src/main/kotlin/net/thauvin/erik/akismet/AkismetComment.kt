/*
 * AkismetComment.kt
 *
 * Copyright (c) 2019-2021, Erik C. Thauvin (erik@thauvin.net)
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

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.servlet.http.HttpServletRequest
import kotlin.collections.HashMap
import kotlin.collections.Map
import kotlin.collections.emptyMap
import kotlin.collections.iterator
import kotlin.collections.set

private fun String?.ifNull() = this ?: ""

/**
 * A comment to send to Akismet.
 *
 * Most everything is optional. Performance can drop dramatically if you choose to exclude data points. The more data
 * you send Akismet about each comment, the greater the accuracy. They recommend erring on the side of including
 * too much data.
 *
 * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
 *
 * @constructor Create an Akismet comment instance.
 *
 * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
 *
 * @param userIp IP address of the comment submitter.
 * @param userAgent User agent string of the web browser submitting the comment.
 */
@Serializable
open class AkismetComment(val userIp: String, val userAgent: String) {
    @Suppress("unused")
    companion object {
        /** A blog comment. */
        const val TYPE_COMMENT = "comment"

        /** A top-level forum post. */
        const val TYPE_FORUM_POST = "forum-post"

        /** A reply to a top-level forum post. */
        const val TYPE_REPLY = "reply"

        /** A blog post. */
        const val TYPE_BLOG_POST = "blog-post"

        /** A contact form or feedback form submission. */
        const val TYPE_CONTACT_FORM = "contact-form"

        /**  A new user account. */
        const val TYPE_SIGNUP = "signup"

        /**  A message sent between just a few users. */
        const val TYPE_MESSAGE = "message"

        /** A pingback. */
        const val TYPE_PINGBACK = "pingback"

        /** A trackback. */
        const val TYPE_TRACKBACK = "trackback"

        /** A Twitter message. */
        const val TYPE_TWEET = "tweet"

        /** Administrator role. If used, Akismet will always return false. */
        const val ADMIN_ROLE = "administrator"
    }

    /** The content of the referer header should be set here. */
    var referrer: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /** The full permanent URL of the entry the comment was submitted to. */
    var permalink: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /**
     * A string that describes the type of content being sent, such as:
     *
     * - [TYPE_COMMENT]
     * - [TYPE_FORUM_POST]
     * - [TYPE_REPLY]
     * - [TYPE_BLOG_POST]
     * - [TYPE_CONTACT_FORM]
     * - [TYPE_SIGNUP]
     * - [TYPE_MESSAGE]
     * - [TYPE_PINGBACK]
     * - [TYPE_TRACKBACK]
     * - [TYPE_TWEET]
     *
     * You may send a value not listed above if none of them accurately describe your content.
     *
     * This is further explained [here](http://blog.akismet.com/2012/06/19/pro-tip-tell-us-your-comment_type/).
     */
    var type: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /** Name submitted with the comment. */
    var author: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /** Email address submitted with the comment. */
    var authorEmail: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /** URL submitted with comment. */
    var authorUrl: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /** The content that was submitted. */
    var content: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /**
     * The UTC timestamp of the creation of the comment, in ISO 8601 format.
     *
     * May be omitted if the comment is sent to the API at the time it is created.
     *
     * @see [Akismet.dateToGmt]
     */
    var dateGmt: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /**
     * The UTC timestamp of the publication time for the post, page or thread on which the comment was posted.
     *
     * @see [Akismet.dateToGmt]
     */
    var postModifiedGmt: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /**
     * Indicates the language(s) in use on the blog or site, in ISO 639-1 format, comma-separated.
     *
     * A site with articles in English and French might use: `en, fr_ca`
     */
    var blogLang: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /**
     * The character encoding for the form values included in comment parameters, such as UTF-8 or ISO-8859-1
     */
    var blogCharset: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /**
     * The user role of the user who submitted the comment. This is an optional parameter.
     *
     * If you set it to [ADMIN_ROLE], Akismet will always return false.
     */
    var userRole: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /** This is an optional parameter. You can use it when submitting test queries to Akismet. */
    var isTest: Boolean = false

    /**
     * If you are sending content to Akismet to be rechecked, such as a post that has been edited or old pending
     * comments that you'd like to recheck, include this parameter with a string describing why the content is
     * being rechecked.
     *
     * For example: `edit`
     */
    var recheckReason: String? = ""
        set(value) {
            field = value.ifNull()
        }

    /**
     * In PHP, there is an array of environmental variables called `$_SERVER` that contains information about the Web
     * server itself as well as a key/value for every HTTP header sent with the request. This data is highly useful to
     * Akismet.
     *
     * How the submitted content interacts with the server can be very telling, so please include as much of it as
     * possible.
     */
    var serverEnv: Map<String, String> = emptyMap()

    /**
     * Create an Akismet comment extracting the [userIp], [userAgent], [referrer] and [serverEnv] environment variables
     * from a Servlet request.
     *
     * See the
     * [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     *
     * @see [serverEnv]
     */
    constructor(request: HttpServletRequest) : this(
        request.remoteAddr,
        request.getHeader("User-Agent").ifNull()
    ) {
        referrer = request.getHeader("referer").ifNull()
        serverEnv = buildServerEnv(request)
    }

    /**
     * Returns a JSON representation of the comment.
     *
     * @see [Akismet.jsonComment]
     */
    fun toJson(): String {
        return toString()
    }

    /**
     * Returns a JSON representation of the comment.
     *
     * @see [Akismet.jsonComment]
     */
    override fun toString(): String {
        return Json.encodeToString(this)
    }

    /**
     * Indicates whether some other object is _equal to_ this one.
     */
    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AkismetComment

        if (userIp != other.userIp) return false
        if (userAgent != other.userAgent) return false
        if (referrer != other.referrer) return false
        if (permalink != other.permalink) return false
        if (type != other.type) return false
        if (author != other.author) return false
        if (authorEmail != other.authorEmail) return false
        if (authorUrl != other.authorUrl) return false
        if (content != other.content) return false
        if (dateGmt != other.dateGmt) return false
        if (postModifiedGmt != other.postModifiedGmt) return false
        if (blogLang != other.blogLang) return false
        if (blogCharset != other.blogCharset) return false
        if (userRole != other.userRole) return false
        if (isTest != other.isTest) return false
        if (recheckReason != other.recheckReason) return false
        if (serverEnv != other.serverEnv) return false

        return true
    }

    /**
     * Returns a hash code value for the object.
     */
    @Suppress("DuplicatedCode")
    override fun hashCode(): Int {
        var result = userIp.hashCode()
        result = 31 * result + userAgent.hashCode()
        result = 31 * result + referrer.hashCode()
        result = 31 * result + permalink.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + authorEmail.hashCode()
        result = 31 * result + authorUrl.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + dateGmt.hashCode()
        result = 31 * result + postModifiedGmt.hashCode()
        result = 31 * result + blogLang.hashCode()
        result = 31 * result + blogCharset.hashCode()
        result = 31 * result + userRole.hashCode()
        result = 31 * result + isTest.hashCode()
        result = 31 * result + recheckReason.hashCode()
        result = 31 * result + serverEnv.hashCode()
        return result
    }
}

private fun buildServerEnv(request: HttpServletRequest): Map<String, String> {
    val params = HashMap<String, String>()

    params["REMOTE_ADDR"] = request.remoteAddr
    params["REQUEST_URI"] = request.requestURI

    for (name in request.headerNames) {
        if (!name.equals("cookie", true)) {
            params["HTTP_${name.toUpperCase().replace('-', '_')}"] = request.getHeader(name).ifNull()
        }
    }

    return params
}
