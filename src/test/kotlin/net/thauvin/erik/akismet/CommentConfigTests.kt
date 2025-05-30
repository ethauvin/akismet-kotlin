/*
 * CommentConfigTests.kt
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

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommentConfigTests {
    @Test
    fun `Default optional fields`() {
        val commentConfig = CommentConfig.Builder("192.168.0.1", "DefaultAgent").build()

        assertEquals("", commentConfig.referrer)
        assertEquals("", commentConfig.permalink)
        assertEquals(CommentType.NONE, commentConfig.type)
        assertEquals("", commentConfig.author)
        assertEquals("", commentConfig.authorEmail)
        assertEquals("", commentConfig.authorUrl)
        assertEquals("", commentConfig.content)
        assertEquals("", commentConfig.dateGmt)
        assertEquals("", commentConfig.postModifiedGmt)
        assertEquals("", commentConfig.blogLang)
        assertEquals("", commentConfig.blogCharset)
        assertEquals("", commentConfig.userRole)
        assertEquals(false, commentConfig.isTest)
        assertEquals("", commentConfig.recheckReason)
        assertTrue(commentConfig.serverEnv.isEmpty())
    }

    @Test
    fun `Empty server environment`() {
        val commentConfig = CommentConfig.Builder("127.0.0.1", "TestUserAgent")
            .serverEnv(emptyMap())
            .build()
        assertTrue(commentConfig.serverEnv.isEmpty())
    }


    @Test
    fun `Invalid inputs for mandatory fields`() {
        try {
            CommentConfig.Builder("", "UserAgent").build()
        } catch (e: IllegalArgumentException) {
            assertEquals("User IP cannot be empty", e.message)
        }

        try {
            CommentConfig.Builder("127.0.0.1", "").build()
        } catch (e: IllegalArgumentException) {
            assertEquals("User Agent cannot be empty", e.message)
        }
    }


    @Nested
    @DisplayName("Builder Tests")
    inner class BuilderTests {
        @Test
        fun `Builder with all optional fields`() {
            val builder = CommentConfig.Builder("127.0.0.1", "TestUserAgent")
                .referrer("http://example.com")
                .permalink("http://example.com/post")
                .type(CommentType.COMMENT)
                .author("John Doe")
                .authorEmail("john.doe@example.com")
                .authorUrl("http://johndoe.com")
                .content("This is a test comment.")
                .dateGmt("2025-05-28T00:00:00Z")
                .postModifiedGmt("2025-05-28T01:00:00Z")
                .blogLang("en")
                .blogCharset("UTF-8")
                .userRole("admin")
                .isTest(true)
                .recheckReason("manual recheck")
                .serverEnv(mapOf("key" to "value"))

            val commentConfig = builder.build()

            assertEquals("127.0.0.1", commentConfig.userIp)
            assertEquals("TestUserAgent", commentConfig.userAgent)
            assertEquals("http://example.com", commentConfig.referrer)
            assertEquals("http://example.com/post", commentConfig.permalink)
            assertEquals(CommentType.COMMENT, commentConfig.type)
            assertEquals("John Doe", commentConfig.author)
            assertEquals("john.doe@example.com", commentConfig.authorEmail)
            assertEquals("http://johndoe.com", commentConfig.authorUrl)
            assertEquals("This is a test comment.", commentConfig.content)
            assertEquals("2025-05-28T00:00:00Z", commentConfig.dateGmt)
            assertEquals("2025-05-28T01:00:00Z", commentConfig.postModifiedGmt)
            assertEquals("en", commentConfig.blogLang)
            assertEquals("UTF-8", commentConfig.blogCharset)
            assertEquals("admin", commentConfig.userRole)
            assertEquals(true, commentConfig.isTest)
            assertEquals("manual recheck", commentConfig.recheckReason)
            assertEquals(mapOf("key" to "value"), commentConfig.serverEnv)
        }

        @Test
        fun `Builder with mandatory fields only`() {
            val builder = CommentConfig.Builder("127.0.0.1", "TestUserAgent")
            val commentConfig = builder.build()

            assertEquals("127.0.0.1", commentConfig.userIp)
            assertEquals("TestUserAgent", commentConfig.userAgent)
            assertEquals("", commentConfig.referrer)
            assertEquals("", commentConfig.permalink)
            assertEquals(CommentType.NONE, commentConfig.type)
            assertEquals("", commentConfig.author)
            assertEquals("", commentConfig.authorEmail)
            assertEquals("", commentConfig.authorUrl)
            assertEquals("", commentConfig.content)
            assertEquals("", commentConfig.dateGmt)
            assertEquals("", commentConfig.postModifiedGmt)
            assertEquals("", commentConfig.blogLang)
            assertEquals("", commentConfig.blogCharset)
            assertEquals("", commentConfig.userRole)
            assertEquals(false, commentConfig.isTest)
            assertEquals("", commentConfig.recheckReason)
            assertTrue(commentConfig.serverEnv.isEmpty())
        }

        @Test
        fun `Builder with modified mandatory fields`() {
            val builder = CommentConfig.Builder("127.0.0.1", "TestUserAgent")
                .userIp("192.168.1.1")
                .userAgent("ModifiedUserAgent")
            val commentConfig = builder.build()

            assertEquals("192.168.1.1", commentConfig.userIp)
            assertEquals("ModifiedUserAgent", commentConfig.userAgent)
        }
    }
}
