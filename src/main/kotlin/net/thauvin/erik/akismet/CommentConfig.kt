/*
 * CommentConfig.kt
 *
 * Copyright 2019-2024 Erik C. Thauvin (erik@thauvin.net)
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

/**
 * Provides a comment configuration.
 */
class CommentConfig private constructor(builder: Builder) {
    val userIp: String = builder.userIp
    val userAgent: String = builder.userAgent
    val referrer = builder.referrer
    val permalink = builder.permalink
    val type = builder.type
    val author = builder.author
    val authorEmail = builder.authorEmail
    val authorUrl = builder.authorUrl
    val content = builder.content
    val dateGmt = builder.dateGmt
    val postModifiedGmt = builder.postModifiedGmt
    val blogLang = builder.blogLang
    val blogCharset = builder.blogCharset
    val userRole = builder.userRole
    val isTest = builder.isTest
    val recheckReason = builder.recheckReason
    val serverEnv = builder.serverEnv

    /**
     * Provides a configuration builder.
     *
     * @param userIp IP address of the comment submitter.
     * @param userAgent User agent string of the web browser submitting the comment.
     */
    data class Builder(var userIp: String, var userAgent: String) {
        var referrer = ""
        var permalink = ""
        var type: CommentType = CommentType.NONE
        var author = ""
        var authorEmail = ""
        var authorUrl = ""
        var content = ""
        var dateGmt = ""
        var postModifiedGmt = ""
        var blogLang = ""
        var blogCharset = ""
        var userRole = ""
        var isTest = false
        var recheckReason = ""
        var serverEnv: Map<String, String> = emptyMap()

        /**
         * Sets the IP address of the comment submitter.
         */
        fun userIp(userIp: String): Builder = apply { this.userIp = userIp }

        /**
         * Sets the user agent string of the web browser submitting the comment.
         */
        fun userAgent(userAgent: String): Builder = apply { this.userAgent = userAgent }

        /**
         * Sets the content of the referrer header.
         */
        fun referrer(referrer: String): Builder = apply { this.referrer = referrer }

        /**
         * Sets the full permanent URL of the entry the comment was submitted to.
         */
        fun permalink(permalink: String): Builder = apply { this.permalink = permalink }

        /**
         * Sets a string that describes the type of content being sent, such as:
         *
         * - [CommentType.COMMENT]
         * - [CommentType.FORUM_POST]
         * - [CommentType.REPLY]
         * - [CommentType.BLOG_POST]
         * - [CommentType.CONTACT_FORM]
         * - [CommentType.SIGNUP]
         * - [CommentType.MESSAGE]
         * - [CommentType.PINGBACK]
         * - [CommentType.TRACKBACK]
         * - [CommentType.TWEET]
         *
         * You may send a value not listed above if none of them accurately describe your content.
         *
         * This is further explained [here](http://blog.akismet.com/2012/06/19/pro-tip-tell-us-your-comment_type/).
         */
        fun type(type: CommentType): Builder = apply { this.type = type }

        /**
         * Sets the mame submitted with the comment.
         */
        fun author(author: String): Builder = apply { this.author = author }

        /**
         * Sets the email address submitted with the comment.
         */
        fun authorEmail(authorEmail: String): Builder = apply { this.authorEmail = authorEmail }

        /**
         * Sets the URL submitted with comment.
         */
        fun authorUrl(authorUrl: String): Builder = apply { this.authorUrl = authorUrl }

        /**
         * Sets the content that was submitted.
         */
        fun content(content: String): Builder = apply { this.content = content }

        /**
         * Sets the UTC timestamp of the creation of the comment, in ISO 8601 format.
         *
         * May be omitted if the comment is sent to the API at the time it is created.
         *
         * @see [Akismet.dateToGmt]
         */
        fun dateGmt(dateGmt: String): Builder = apply { this.dateGmt = dateGmt }

        /**
         * Sets the UTC timestamp of the publication time for the post, page or thread on which the comment was posted.
         *
         * @see [Akismet.dateToGmt]
         */
        fun postModifiedGmt(postModifiedGmt: String): Builder = apply { this.postModifiedGmt = postModifiedGmt }

        /**
         * Indicates the language(s) in use on the blog or site, in ISO 639-1 format, comma-separated.
         *
         * A site with articles in English and French might use: `en, fr_ca`
         */
        fun blogLang(blogLang: String): Builder = apply { this.blogLang = blogLang }

        /**
         * Sets the character encoding for the form values included in comment parameters, such as UTF-8 or ISO-8859-1
         */
        fun blogCharset(blogCharset: String): Builder = apply { this.blogCharset = blogCharset }

        /**
         * Set the user role of the user who submitted the comment. This is an optional parameter.
         *
         * If you set it to [AkismetComment.ADMIN_ROLE], Akismet will always return `false`.
         */
        fun userRole(userRole: String): Builder = apply { this.userRole = userRole }

        /**
         * This is optional. You can set it when submitting test queries to Akismet.
         */
        fun isTest(isTest: Boolean): Builder = apply { this.isTest = isTest }

        /**
         * If you are sending content to Akismet to be rechecked, such as a post that has been edited or old pending
         * comments that you'd like to recheck, include this parameter with a string describing why the content is
         * being rechecked.
         *
         * For example: `edit`
         */
        fun recheckReason(checkReason: String): Builder = apply { this.recheckReason = checkReason }

        /**
         * In PHP, there is an array of environmental variables called `$_SERVER` that contains information about the
         * Web server itself as well as a key/value for every HTTP header sent with the request. This data is highly
         * useful to Akismet.
         *
         * How the submitted content interacts with the server can be very telling, so please include as much of it as
         * possible.
         */
        fun serverEnv(serverEnv: Map<String, String>): Builder = apply { this.serverEnv = serverEnv }

        /**
         * Builds a new comment configuration.
         */
        fun build(): CommentConfig = CommentConfig(this)
    }
}
