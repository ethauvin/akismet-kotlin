/*
 * CommentType.kt
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

import kotlinx.serialization.Serializable

/**
 * Defines the comment types.
 */
@Serializable
data class CommentType(var value: String) {
    companion object {
        /**
         * A blog comment.
         */
        @JvmField
        val COMMENT = CommentType("comment")

        /**
         * A top-level forum post.
         */
        @JvmField
        val FORUM_POST = CommentType("forum-post")

        /**
         * A reply to a top-level forum post.
         */
        @JvmField
        val REPLY = CommentType("reply")

        /**
         * A blog post.
         */
        @JvmField
        val BLOG_POST = CommentType("blog-post")

        /**
         * A contact form or feedback form submission.
         */
        @JvmField
        val CONTACT_FORM = CommentType("contact-form")

        /** A new user account.
         */
        @JvmField
        val SIGNUP = CommentType("signup")

        /**
         * A message sent between just a few users.
         */
        @JvmField
        val MESSAGE = CommentType("message")

        /**
         * A pingback.
         */
        @JvmField
        val PINGBACK = CommentType("pingback")

        /**
         * A trackback.
         */
        @JvmField
        val TRACKBACK = CommentType("trackback")

        /**
         * A Twitter message.
         */
        @JvmField
        val TWEET = CommentType("tweet")

        /**
         * Undefined type.
         */
        @JvmField
        val NONE = CommentType("")
    }
}
