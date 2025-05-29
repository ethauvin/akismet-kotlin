/*
 * AkismetTests.kt
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
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertThrows
import org.junit.BeforeClass
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * [Akismet] Tests
 *
 * `AKISMET_API_KEY` and `AKISMET_BLOG` should be set in env vars or `local.properties`
 */
class AkismetTests {
    private val emptyFormBody = FormBody.Builder().build()

    companion object {
        private const val REFERER = "https://www.google.com"
        private val apiKey = getKey("AKISMET_API_KEY")
        private val blog = getKey("AKISMET_BLOG")
        private val akismet = Akismet(apiKey, blog)
        private val comment = AkismetComment(
            userIp = "127.0.0.1",
            userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
        )
        private val date = Date()
        private val config = CommentConfig.Builder(comment.userIp, comment.userAgent)
            .referrer(REFERER)
            .permalink("https://yourblogdomainname.com/blog/post=1")
            .type(CommentType.COMMENT)
            .author("admin")
            .authorEmail("test@test.com")
            .authorUrl("https://www.CheckOutMyCoolSite.com")
            .content("It means a lot that you would take the time to review our software. Thanks again.")
            .dateGmt(Akismet.dateToGmt(date))
            .postModifiedGmt(Akismet.dateToGmt(date))
            .blogLang("en")
            .blogCharset("UTF-8")
            .userRole(AkismetComment.ADMIN_ROLE)
            .recheckReason("edit")
            .isTest(true)
            .build()
        private val mockComment: AkismetComment = AkismetComment(request = getMockRequest())
        private val isFirstRun = AtomicBoolean(true)

        init {
            with(comment) {
                referrer = config.referrer
                permalink = config.permalink
                type = CommentType("comment")
                author = config.author
                authorEmail = config.authorEmail
                authorUrl = config.authorUrl
                content = config.content
                dateGmt = config.dateGmt
                postModifiedGmt = config.postModifiedGmt
                blogLang = config.blogLang
                blogCharset = config.blogCharset
                userRole = config.userRole
                recheckReason = config.recheckReason
                isTest = config.isTest
            }

            with(mockComment) {
                permalink = comment.permalink
                type = comment.type
                authorEmail = comment.authorEmail
                author = comment.author
                authorUrl = comment.authorUrl
                content = comment.content
                dateGmt = comment.dateGmt
                postModifiedGmt = comment.dateGmt
                blogLang = comment.blogLang
                blogCharset = comment.blogCharset
                userRole = AkismetComment.ADMIN_ROLE
                recheckReason = comment.recheckReason
                isTest = true
            }
        }

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            if (isFirstRun.getAndSet(false)) {
                with(akismet.logger) {
                    addHandler(ConsoleHandler().apply { level = Level.FINE })
                    level = Level.FINE
                }
            }

            akismet.logger.info(comment.toString())
            akismet.logger.info(mockComment.toJson())
        }

        private fun getKey(key: String): String {
            return System.getenv(key)?.takeUnless { it.isBlank() }
                ?: loadPropertyValue(key)
        }

        private fun getMockRequest(): HttpServletRequest {
            val request = Mockito.mock(HttpServletRequest::class.java)
            with(request) {
                whenever(remoteAddr).thenReturn(comment.userIp)
                whenever(requestURI).thenReturn("/blog/post=1")
                whenever(getHeader("referer")).thenReturn(REFERER)
                whenever(getHeader("Cookie")).thenReturn("name=value; name2=value2; name3=value3")
                whenever(getHeader("User-Agent")).thenReturn(comment.userAgent)
                whenever(getHeader("Accept-Encoding")).thenReturn("gzip")
                whenever(headerNames).thenReturn(
                    Collections.enumeration(listOf("User-Agent", "referer", "Cookie", "Accept-Encoding", "Null"))
                )
            }
            return request
        }

        private fun loadPropertyValue(key: String): String {
            return File("local.properties")
                .takeIf { it.exists() }
                ?.let { file ->
                    FileInputStream(file).use { fis ->
                        Properties().apply { load(fis) }.getProperty(key, "")
                    }
                }.orEmpty()
        }
    }

    @Nested
    @DisplayName("Comment Tests")
    inner class CommentTests {
        @Test
        fun checkComment() {
            with(akismet) {
                assertFalse(checkComment(comment), "checkComment(admin)")
                assertThat(akismet::response).isEqualTo("false")

                comment.userRole = ""
                assertTrue(checkComment(comment), "checkComment()")
                assertThat(akismet::response).isEqualTo("true")

                assertFalse(checkComment(mockComment), "checkComment(mock)")
                assertThat(akismet::response).isEqualTo("false")

                mockComment.userRole = ""
                assertTrue(checkComment(mockComment), "checkComment(mock)")
                assertThat(akismet::response).isEqualTo("true")

                assertThat(akismet::httpStatusCode).isEqualTo(200)

                comment.userRole = AkismetComment.ADMIN_ROLE
            }
        }

        @Test
        fun emptyComment() {
            assertThrows(
                java.lang.IllegalArgumentException::class.java
            ) { akismet.checkComment(AkismetComment("", "")) }


            val empty = AkismetComment("", "")
            assertThat(empty, "AkismetComment(empty)").all {
                prop(AkismetComment::isTest).isFalse()
                prop(AkismetComment::referrer).isEqualTo("")
                prop(AkismetComment::permalink).isEqualTo("")
                prop(AkismetComment::type).isEqualTo(CommentType.NONE)
                prop(AkismetComment::authorEmail).isEqualTo("")
                prop(AkismetComment::author).isEqualTo("")
                prop(AkismetComment::authorUrl).isEqualTo("")
                prop(AkismetComment::content).isEqualTo("")
                prop(AkismetComment::dateGmt).isEqualTo("")
                prop(AkismetComment::postModifiedGmt).isEqualTo("")
                prop(AkismetComment::blogLang).isEqualTo("")
                prop(AkismetComment::blogCharset).isEqualTo("")
                prop(AkismetComment::userRole).isEqualTo("")
                prop(AkismetComment::recheckReason).isEqualTo("")
                prop(AkismetComment::serverEnv).size().isEqualTo(0)
            }

            with(receiver = empty) {
                for (s in listOf("test", "", null)) {
                    referrer = s
                    permalink = s
                    if (s != null) type = CommentType(s)
                    authorEmail = s
                    author = s
                    authorUrl = s
                    content = s
                    dateGmt = s
                    postModifiedGmt = s
                    blogLang = s
                    blogCharset = s
                    userRole = s
                    recheckReason = s

                    val expected = if (s.isNullOrEmpty()) "" else s

                    assertThat(empty, "AkismetComment($s)").all {
                        prop(AkismetComment::referrer).isEqualTo(expected)
                        prop(AkismetComment::permalink).isEqualTo(expected)
                        prop(AkismetComment::type).isEqualTo(CommentType(expected))
                        prop(AkismetComment::authorEmail).isEqualTo(expected)
                        prop(AkismetComment::author).isEqualTo(expected)
                        prop(AkismetComment::authorUrl).isEqualTo(expected)
                        prop(AkismetComment::content).isEqualTo(expected)
                        prop(AkismetComment::dateGmt).isEqualTo(expected)
                        prop(AkismetComment::postModifiedGmt).isEqualTo(expected)
                        prop(AkismetComment::blogLang).isEqualTo(expected)
                        prop(AkismetComment::blogCharset).isEqualTo(expected)
                        prop(AkismetComment::userRole).isEqualTo(expected)
                        prop(AkismetComment::recheckReason).isEqualTo(expected)
                        prop(AkismetComment::serverEnv).size().isEqualTo(0)
                    }
                }
            }
        }

        @Test
        fun mockComment() {
            assertThat(mockComment, "mockComment").all {
                prop(AkismetComment::userIp).isEqualTo(comment.userIp)
                prop(AkismetComment::userAgent).isEqualTo(comment.userAgent)
                prop(AkismetComment::referrer).isEqualTo(comment.referrer)
                prop(AkismetComment::serverEnv).all {
                    key("HTTP_ACCEPT_ENCODING").isEqualTo("gzip")
                    key("REMOTE_ADDR").isEqualTo(comment.userIp)
                    key("HTTP_NULL").isEmpty()
                    size().isEqualTo(6)
                }
            }
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    inner class ConstructorTests {
        @Test
        fun apiKeyTooLong() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("123456789 12")
            }
        }

        @Test
        fun apiKeyTooShort() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("1234")
            }
        }

        @Test
        fun invalidKeyAndBlog() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("1234", "foo")
            }
        }

        @Test
        fun noApiKey() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("")
            }
        }

        @Test
        fun noBlog() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("123456789012", "")
            }
        }
    }

    @Test
    fun dateToGmt() {
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        val utcDate = Akismet.dateToGmt(date)
        assertEquals(Akismet.dateToGmt(localDateTime), utcDate, "dateGmt(localDateTime)")
        assertThat(comment::dateGmt).isEqualTo(utcDate)
    }

    @Nested
    @DisplayName("JSON Comment Tests")
    inner class JsonCommentTest {
        @Test
        fun jsonCommentEqualHashCode() {
            val jsonComment = Akismet.jsonComment(mockComment.toJson())
            assertEquals(
                jsonComment.hashCode(),
                mockComment.hashCode(),
                "jsonComment.hashCode = mockComment.hashcode"
            )
        }

        @Test
        fun jsonCommentEqualsMockComment() {
            val jsonComment = Akismet.jsonComment(mockComment.toJson())
            assertEquals(jsonComment, mockComment, "jsonComment = mockComment")
        }

        @Test
        fun jsonCommentNotEqualsComment() {
            val jsonComment = Akismet.jsonComment(mockComment.toJson())
            assertNotEquals(jsonComment, comment, "json")
            assertNotEquals(
                jsonComment.hashCode(),
                comment.hashCode(),
                "jsonComment.hashCode != mockComment.hashcode"
            )
        }

        @Test
        fun jsonCommentNotEqualsMockComment() {
            val jsonComment = Akismet.jsonComment(mockComment.toJson())
            jsonComment.recheckReason = ""
            assertNotEquals(jsonComment, mockComment, "jsonComment != jsonComment")
        }

        @Test
        fun jsonCommentNotEqualsThis() {
            Akismet.jsonComment(mockComment.toJson())
            assertThat(this, "this != comment").isNotEqualTo(comment)
        }
    }

    @Nested
    @DisplayName("Response Tests")
    inner class ResponseTests {
        @Test
        fun emptyResponse() {
            assertTrue(
                akismet.executeMethod(
                    "https://postman-echo.com/status/200".toHttpUrl(), emptyFormBody, true
                )
            )
            var expected = "{\n  \"status\": 200\n}"
            assertThat(akismet, "executeMethod(200)").all {
                prop(Akismet::response).isEqualTo(expected)
                prop(Akismet::errorMessage).contains(expected)
            }

            akismet.reset()
            assertThat(akismet, "akismet.reset()").all {
                prop(Akismet::httpStatusCode).isEqualTo(0)
                prop(Akismet::errorMessage).isEmpty()
            }

            assertTrue(
                akismet.executeMethod(
                    "https://erik.thauvin.net/blank.html".toHttpUrl(), emptyFormBody, true
                )
            )
            expected = ""
            assertThat(akismet, "executeMethod(blank)").all {
                prop(Akismet::response).isEqualTo(expected)
                prop(Akismet::errorMessage).contains("blank")
            }
        }

        @Test
        fun executeMethod() {
            akismet.executeMethod(
                "https://$apiKey.rest.akismet.com/1.1/comment-check".toHttpUrl(),
                FormBody.Builder().apply { add("is_test", "1") }.build()
            )
            assertThat(akismet::debugHelp).isNotEmpty()

            akismet.reset()
            assertThat(akismet, "akismet.reset()").all {
                prop(Akismet::httpStatusCode).isEqualTo(0)
                prop(Akismet::debugHelp).isEmpty()
                prop(Akismet::response).isEmpty()
            }
        }

        @Test
        fun invalidApi() {
            assertThrows(
                java.lang.IllegalArgumentException::class.java
            ) { akismet.executeMethod("https://.com".toHttpUrl(), emptyFormBody) }
        }

        @Test
        fun ioError() {
            akismet.executeMethod("https://www.foobarxyz.com".toHttpUrl(), emptyFormBody)
            assertThat(akismet::errorMessage).contains("IO error")
        }

        @Test
        fun proTip() {
            assertFalse(
                akismet.executeMethod(
                    "https://postman-echo.com/response-headers?x-akismet-pro-tip=discard".toHttpUrl(),
                    emptyFormBody
                )
            )
            assertThat(akismet, "executeMethod(x-akismet-pro-tip=discard)").all {
                prop(Akismet::proTip).isEqualTo("discard")
                prop(Akismet::isDiscard).isTrue()
            }

            akismet.reset()
            assertThat(akismet, "akismet.reset()").all {
                prop(Akismet::isDiscard).isFalse()
                prop(Akismet::response).isEmpty()
                prop(Akismet::httpStatusCode).isEqualTo(0)
            }
        }
    }

    @Nested
    @DisplayName("Submit Test")
    inner class SubmitTests {
        @Test
        fun submitHam() {
            assertTrue(akismet.submitHam(comment), "submitHam")
        }

        @Test
        fun submitHamMocked() {
            assertTrue(akismet.submitHam(mockComment), "submitHam(mock)")
        }

        @Test
        fun submitSpam() {
            assertTrue(akismet.submitSpam(comment), "submitHam")
        }

        @Test
        fun submitSpamMocked() {
            assertTrue(akismet.submitSpam(mockComment), "submitHam(mock)")
        }
    }

    @Nested
    @DisplayName("User Agent Tests")
    inner class UserAgentTests {
        val libAgent = "${GeneratedVersion.PROJECT}/${GeneratedVersion.VERSION}"

        @Test
        fun userAgentCustom() {
            akismet.appUserAgent = "My App/1.0"

            assertEquals(
                akismet.buildUserAgent(),
                "${akismet.appUserAgent} | $libAgent",
                "buildUserAgent(My App/1.0)"
            )
        }

        @Test
        fun userAgentDefault() {
            assertEquals(akismet.buildUserAgent(), libAgent, "buildUserAgent($libAgent)")
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    inner class ValidationTests {
        @Test
        fun blogProperty() {
            assertThrows(IllegalArgumentException::class.java) {
                akismet.blog = ""
            }

            assertThat(akismet::blog).isEqualTo(blog)
        }

        @Test
        fun validateConfig() {
            assertThat(AkismetComment(config)).isEqualTo(comment)
        }

        @Test
        fun verifyKey() {
            assertThat(akismet, "akismet").all {
                prop(Akismet::isVerifiedKey).isFalse()
                prop(Akismet::verifyKey).isTrue()
                prop(Akismet::response).isEqualTo("valid")
                prop(Akismet::isVerifiedKey).isTrue()
            }

            akismet.reset()
            assertThat(akismet, "akismet.reset()").all {
                prop(Akismet::isVerifiedKey).isFalse()
                prop(Akismet::response).isEmpty()
                prop(Akismet::httpStatusCode).isEqualTo(0)
            }

            assertThat(Akismet("123456789012"), "akismet(123456789012)")
                .prop(Akismet::verifyKey)
                .isFalse()
        }
    }
}
