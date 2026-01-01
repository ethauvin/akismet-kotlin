/*
 * AkismetCommentTest.kt
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

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import rife.bld.extension.testing.LoggingExtension
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertTrue

@ExtendWith(LoggingExtension::class)
class AkismetCommentTest {
    companion object {
        @JvmField
        @RegisterExtension
        val loggingExtension = LoggingExtension(Akismet.logger)
    }

    private val apiKey = TestUtils.getKey("AKISMET_API_KEY")
    private val blog = TestUtils.getKey("AKISMET_BLOG")
    private val config = CommentConfig.Builder(
        userIp = "127.0.0.1",
        userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
    )
        .referrer("https://www.google.com")
        .permalink("https://yourblogdomainname.com/blog/post=1")
        .type(CommentType.COMMENT)
        .author("admin")
        .authorEmail("test@test.com")
        .authorUrl("https://www.CheckOutMyCoolSite.com")
        .content("It means a lot that you would take the time to review our software. Thanks again.")
        .dateGmt(Akismet.dateToGmt(LocalDateTime.of(2025, 5, 29, 0, 0, 0)))
        .postModifiedGmt(Akismet.dateToGmt(LocalDateTime.of(2025, 5, 29, 1, 0, 0)))
        .blogLang("en")
        .blogCharset("UTF-8")
        .userRole(AkismetComment.ADMIN_ROLE)
        .recheckReason("edit")
        .isTest(true)
        .build()


    @Test
    fun `toJson returns correct JSON representation`() {
        val comment = AkismetComment("127.0.0.1", "TestAgent")
        comment.referrer = "https://example.com"
        comment.permalink = "https://permalink.com"
        comment.type = CommentType.COMMENT
        comment.author = "Author"
        comment.content = "Sample comment"

        val json = comment.toJson()
        assertEquals(Json.encodeToString(comment), json)
        assertThat(comment).prop(AkismetComment::toString).isEqualTo(json)
    }

    @Test
    fun `Equals and hashCode methods work as expected`() {
        val comment1 = AkismetComment("127.0.0.1", "TestAgent")
        val comment2 = AkismetComment("127.0.0.1", "TestAgent")
        val comment3 = AkismetComment("192.168.0.1", "OtherAgent")

        assertEquals(comment1, comment2)
        assertNotEquals(comment1, comment3)
        assertEquals(comment1.hashCode(), comment2.hashCode())
        assertNotEquals(comment1.hashCode(), comment3.hashCode())
    }

    @Test
    fun `Property setters handle null values correctly`() {
        val comment = AkismetComment("127.0.0.1", "TestAgent")

        comment.referrer = null
        comment.permalink = null
        comment.author = null
        comment.authorEmail = null
        comment.authorUrl = null
        comment.content = null
        comment.dateGmt = null
        comment.postModifiedGmt = null
        comment.blogLang = null
        comment.blogCharset = null
        comment.userRole = null
        comment.recheckReason = null

        assertThat(comment).all {
            prop(AkismetComment::referrer).isEqualTo("")
            prop(AkismetComment::permalink).isEqualTo("")
            prop(AkismetComment::author).isEqualTo("")
            prop(AkismetComment::authorEmail).isEqualTo("")
            prop(AkismetComment::authorUrl).isEqualTo("")
            prop(AkismetComment::content).isEqualTo("")
            prop(AkismetComment::dateGmt).isEqualTo("")
            prop(AkismetComment::postModifiedGmt).isEqualTo("")
            prop(AkismetComment::blogLang).isEqualTo("")
            prop(AkismetComment::blogCharset).isEqualTo("")
            prop(AkismetComment::userRole).isEqualTo("")
            prop(AkismetComment::recheckReason).isEqualTo("")
            prop(AkismetComment::isTest).isFalse()
            prop(AkismetComment::serverEnv).isEmpty()
        }
    }

    @Nested
    @DisplayName("Check Comment Tests")
    inner class CheckCommentTests {
        @Test
        fun `Check comment with admin role`() {
            val akismet = Akismet(apiKey, blog)
            val comment = AkismetComment(config).apply {
                userRole = AkismetComment.ADMIN_ROLE
                isTest = true
            }
            assertThat(akismet.checkComment(comment), "checkComment()").isFalse()
            assertThat(akismet).prop(Akismet::response).isEqualTo("false")
        }

        @Test
        fun `Check comment with no user role`() {
            val akismet = Akismet(apiKey, blog)
            val comment = AkismetComment(config).apply {
                userRole = ""
                isTest = true
            }
            assertTrue(akismet.checkComment(comment), "checkComment()")
            assertThat(akismet).prop(Akismet::response).isEqualTo("true")
        }

        @Test
        fun `Check comment with no user IP or user agent`() {
            val akismet = Akismet(apiKey, blog)
            assertThrows(
                java.lang.IllegalArgumentException::class.java
            ) { akismet.checkComment(AkismetComment("", "")) }
        }

    }

    @Nested
    @DisplayName("Constructor Tests")
    inner class ConstructorTests {
        @Test
        fun `Constructor with userIp and userAgent initializes fields correctly`() {
            val comment = AkismetComment("127.0.0.1", "TestAgent")

            assertThat(comment).all {
                prop(AkismetComment::userIp).isEqualTo("127.0.0.1")
                prop(AkismetComment::userAgent).isEqualTo("TestAgent")
                prop(AkismetComment::referrer).isEqualTo("")
                prop(AkismetComment::permalink).isEqualTo("")
                prop(AkismetComment::type).isEqualTo(CommentType.NONE)
            }
        }

        @Test
        fun `Constructor with HttpServletRequest initializes fields correctly`() {
            val request = mock(HttpServletRequest::class.java)
            whenever(request.remoteAddr).thenReturn("192.168.0.1")
            whenever(request.getHeader("User-Agent")).thenReturn("MockAgent")
            whenever(request.getHeader("referer")).thenReturn("https://example.com")
            whenever(request.requestURI).thenReturn("/test-uri")
            whenever(request.headerNames)
                .thenReturn(Collections.enumeration(listOf("header1", "header2", "cookie")))
            whenever(request.getHeader("header1")).thenReturn("value1")
            whenever(request.getHeader("header2")).thenReturn("value2")
            whenever(request.getHeader("cookie")).thenReturn("foo")

            val comment = AkismetComment(request)

            assertThat(comment).all {
                prop(AkismetComment::userIp).isEqualTo("192.168.0.1")
                prop(AkismetComment::userAgent).startsWith("MockAgent")
                prop(AkismetComment::referrer).isEqualTo("https://example.com")
                prop(AkismetComment::serverEnv).isEqualTo(
                    mapOf(
                        "REMOTE_ADDR" to "192.168.0.1",
                        "REQUEST_URI" to "/test-uri",
                        "HTTP_HEADER1" to "value1",
                        "HTTP_HEADER2" to "value2"
                    )
                )
            }
        }

        @Test
        fun `Constructor with CommentConfig initializes fields correctly`() {
            val comment = AkismetComment(config).apply {
                serverEnv = mapOf("key" to "value")
            }

            assertThat(comment).all {
                prop(AkismetComment::userIp).isEqualTo("127.0.0.1")
                prop(AkismetComment::userAgent).startsWith("Mozilla/5.0")
                prop(AkismetComment::referrer).isEqualTo("https://www.google.com")
                prop(AkismetComment::permalink).isEqualTo("https://yourblogdomainname.com/blog/post=1")
                prop(AkismetComment::type).isEqualTo(CommentType.COMMENT)
                prop(AkismetComment::author).isEqualTo("admin")
                prop(AkismetComment::authorEmail).isEqualTo("test@test.com")
                prop(AkismetComment::authorUrl).isEqualTo("https://www.CheckOutMyCoolSite.com")
                prop(AkismetComment::content)
                    .isEqualTo("It means a lot that you would take the time to review our software. Thanks again.")
                prop(AkismetComment::dateGmt).isNotNull().matches("2025-05-29T00:00:00(-07:00|Z)".toRegex())
                prop(AkismetComment::postModifiedGmt).isNotNull().matches("2025-05-29T01:00:00(-07:00|Z)".toRegex())
                prop(AkismetComment::blogLang).isEqualTo("en")
                prop(AkismetComment::blogCharset).isEqualTo("UTF-8")
                prop(AkismetComment::userRole).isEqualTo(AkismetComment.ADMIN_ROLE)
                prop(AkismetComment::recheckReason).isEqualTo("edit")
                prop(AkismetComment::isTest).isEqualTo(true)
                prop(AkismetComment::serverEnv).isEqualTo(mapOf("key" to "value"))
            }
        }
    }

    @Nested
    @DisplayName("Submit Test")
    inner class SubmitTests {
        val akismet = Akismet(apiKey, blog)
        val comment = AkismetComment(config).apply {
            isTest = true
            userRole = AkismetComment.ADMIN_ROLE
        }

        @Test
        fun `Submit ham`() {
            assertTrue(akismet.submitHam(comment), "submitHam")
        }

        @Test
        fun `Submit spam`() {
            assertTrue(akismet.submitSpam(comment), "submitSpam")
        }
    }
}
