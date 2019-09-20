/*
 * AkismetComment.kt
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

import javax.servlet.http.HttpServletRequest

/**
 * A comment to send to Akismet.
 *
 * @constructor Create an Akismet comment instance. See the
 * [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
 */
open class AkismetComment() {
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
        /** Administrator role. If used, Akismet will always return false. */
        const val ADMIN_ROLE = "administrator"
    }

    /** IP address of the comment submitter. */
    var userIp: String = ""
    /** User agent string of the web browser submitting the comment. */
    var userAgent: String = ""
    /** The content of the referer header should be set here. */
    var referrer: String = ""
    /** The full permanent URL of the entry the comment was submitted to. */
    var permalink: String = ""
    /**
     * A string that describes the type of content being sent, such as  [TYPE_COMMENT], [TYPE_FORUM_POST], [TYPE_REPLY],
     * [TYPE_BLOG_POST], [TYPE_CONTACT_FORM], [TYPE_SIGNUP], or [TYPE_MESSAGE].
     *
     * You may send a value not listed above if none of them accurately describe your content.
     *
     * This is further explained [here](http://blog.akismet.com/2012/06/19/pro-tip-tell-us-your-comment_type/).
     */
    var type: String = ""
    /** Name submitted with the comment. */
    var author: String = ""
    /** Email address submitted with the comment. */
    var authorEmail: String = ""
    /** URL submitted with comment. */
    var authorUrl: String = ""
    /** The content that was submitted. */
    var content: String = ""
    /**
     * The UTC timestamp of the creation of the comment, in ISO 8601 format.
     *
     * May be omitted if the comment is sent to the API at the time it is created.
     *
     * @see [Akismet.dateToGmt]
     */
    var dateGmt: String = ""
    /** The UTC timestamp of the publication time for the post, page or thread on which the comment was posted. */
    var postModifiedGmt: String = ""
    /**
     * Indicates the language(s) in use on the blog or site, in ISO 639-1 format, comma-separated.
     *
     * A site with articles in English and French might use: ```en, fr_ca```
     */
    var blogLang: String = ""
    /**
     * The character encoding for the form values included in comment parameters, such as UTF-8 or ISO-8859-1
     */
    var blogCharset: String = ""
    /**
     * The user role of the user who submitted the comment. This is an optional parameter.
     *
     * If you set it to [ADMIN_ROLE], Akismet will always return false.
     */
    var userRole: String = ""
    /** This is an optional parameter. You can use it when submitting test queries to Akismet. */
    var isTest: Boolean = false
    /**
     * If you are sending content to Akismet to be rechecked, such as a post that has been edited or old pending
     * comments that you'd like to recheck, include this parameter with a string describing why the content is
     * being rechecked.
     *
     * For example: ```edit```
     */
    var recheckReason: String = ""
    /**
     * In PHP, there is an array of environmental variables called $_SERVER that contains information about the Web
     * server itself as well as a key/value for every HTTP header sent with the request.
     *
     * This data is highly useful to Akismet. How the submitted content interacts with the server can be very telling,
     * so please include as much of it as possible.
     */
    var other: Map<String, String> = emptyMap()

    /**
     * Create an Akismet comment instance.
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     */
    constructor(
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
    ) : this() {
        this.userIp = userIp
        this.userAgent = userAgent
        this.referrer = referrer
        this.permalink = permalink
        this.type = type
        this.author = author
        this.authorEmail = authorEmail
        this.authorUrl = authorUrl
        this.content = content
        this.dateGmt = dateGmt
        this.postModifiedGmt = postModifiedGmt
        this.blogLang = blogLang
        this.blogCharset = blogCharset
        this.userRole = userRole
        this.isTest = isTest
        this.recheckReason = recheckReason
        this.other = other
    }

    /**
     * Create Akismet comment extracting [userIp], [userAgent], [referrer] and [other] variables from a
     * [HttpServletRequest](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServletRequest.html).
     * See the [Akismet API](https://akismet.com/development/api/#comment-check) for more details.
     */
    constructor(
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
    ) : this(
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

    if (other.isNotEmpty()) {
        params.putAll(other)
    }

    return params
}
