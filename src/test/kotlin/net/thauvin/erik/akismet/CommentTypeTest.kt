/*
 * CommentTypeTest.kt
 *
 * Copyright 2019-2026 Erik C. Thauvin (erik@thauvin.net)
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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommentTypeTest {
    @Test
    fun `Verify BLOG_POST value`() {
        val commentType = CommentType.BLOG_POST
        assertEquals("blog-post", commentType.value)
    }

    @Test
    fun `Verify COMMENT value`() {
        val commentType = CommentType.COMMENT
        assertEquals("comment", commentType.value)
    }

    @Test
    fun `Verify CONTACT_FORM value`() {
        val commentType = CommentType.CONTACT_FORM
        assertEquals("contact-form", commentType.value)
    }

    @Test
    fun `Verify FORUM_POST value`() {
        val commentType = CommentType.FORUM_POST
        assertEquals("forum-post", commentType.value)
    }

    @Test
    fun `Verify MESSAGE value`() {
        val commentType = CommentType.MESSAGE
        assertEquals("message", commentType.value)
    }

    @Test
    fun `Verify NONE value`() {
        val commentType = CommentType.NONE
        assertEquals("", commentType.value)
    }

    @Test
    fun `Verify PINGBACK value`() {
        val commentType = CommentType.PINGBACK
        assertEquals("pingback", commentType.value)
    }

    @Test
    fun `Verify REPLY value`() {
        val commentType = CommentType.REPLY
        assertEquals("reply", commentType.value)
    }

    @Test
    fun `Verify SIGNUP value`() {
        val commentType = CommentType.SIGNUP
        assertEquals("signup", commentType.value)
    }

    @Test
    fun `Verify TRACKBACK value`() {
        val commentType = CommentType.TRACKBACK
        assertEquals("trackback", commentType.value)
    }

    @Test
    fun `Verify TWEET value`() {
        val commentType = CommentType.TWEET
        assertEquals("tweet", commentType.value)
    }
}
