/*
 * AkismetTest.kt
 *
 * Copyright 2019-2023 Erik C. Thauvin (erik@thauvin.net)
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
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


fun getKey(key: String): String {
    var value = System.getenv(key) ?: ""
    if (value.isBlank()) {
        val localProps = File("local.properties")
        if (localProps.exists())
            localProps.apply {
                if (exists()) {
                    FileInputStream(this).use { fis ->
                        Properties().apply {
                            load(fis)
                            value = getProperty(key, "")
                        }
                    }
                }
            }
    }
    return value
}

/**
 * AKISMET_API_KEY and AKISMET_BLOG should be in env vars or local.properties
 */
class AkismetTest {
    private val emptyFormBody = FormBody.Builder().build()

    @Test
    fun constructorsTest() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Akismet("")
        }
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Akismet("1234")
        }
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Akismet("123456789 12")
        }
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Akismet("123456789012", "")
        }
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Akismet("1234", "foo")
        }
    }

    @Test
    fun blogPropertyTest() {
        assertThrows(IllegalArgumentException::class.java) {
            akismet.blog = ""
        }

        assertThat(akismet::blog).isEqualTo(blog)
    }

    @Test
    fun verifyKeyTest() {
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

        assertThat(Akismet("123456789012"), "akismet(123456789012)").prop(Akismet::verifyKey).isFalse()
    }

    @Test
    fun mockCommentTest() {
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

    @Test
    fun emptyCommentTest() {
        assertThrows(
            java.lang.IllegalArgumentException::class.java
        ) { akismet.checkComment(AkismetComment("", "")) }


        val empty = AkismetComment("", "")
        assertThat(empty, "AkismetComment(empty)").all {
            prop(AkismetComment::isTest).isFalse()
            prop(AkismetComment::referrer).isEqualTo("")
            prop(AkismetComment::permalink).isEqualTo("")
            prop(AkismetComment::type).isEqualTo("")
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

        with(empty) {
            for (s in listOf("test", "", null)) {
                referrer = s
                permalink = s
                type = s
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
                    prop(AkismetComment::type).isEqualTo(expected)
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
    fun emptyResponseTest() {
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
    fun proTipResponseTest() {
        assertFalse(
            akismet.executeMethod(
                "https://postman-echo.com/response-headers?x-akismet-pro-tip=discard".toHttpUrl(),
                emptyFormBody
            )
        )

        assertThat(akismet, "executeMethod(pro-tip)").all {
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

    @Test
    fun checkCommentTest() {
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
        }
    }

    @Test
    fun executeMethodTest() {
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
    fun invalidApiTest() {
        assertThrows(
            java.lang.IllegalArgumentException::class.java
        ) { akismet.executeMethod("https://.com".toHttpUrl(), emptyFormBody) }

    }

    @Test
    fun ioErrorTest() {
        akismet.executeMethod("https://www.foobarxyz.com".toHttpUrl(), emptyFormBody)
        assertThat(akismet::errorMessage).contains("IO error")
    }

    @Test
    fun submitHamTest() {
        assertTrue(akismet.submitHam(comment), "submitHam")

        assertTrue(akismet.submitHam(mockComment), "submitHam(mock)")
    }

    @Test
    fun submitSpamTest() {
        assertTrue(akismet.submitSpam(comment), "submitHam")
        assertTrue(akismet.submitSpam(mockComment), "submitHam(mock)")
    }

    @Test
    fun jsonCommentTest() {
        val jsonComment = Akismet.jsonComment(mockComment.toJson())

        assertEquals(jsonComment, mockComment, "jsonComment = mockComment")
        assertEquals(jsonComment.hashCode(), mockComment.hashCode(), "jsonComment.hashCode = mockComment.hashcode")

        assertNotEquals(jsonComment, comment, "json")
        assertNotEquals(jsonComment.hashCode(), comment.hashCode(), "jsonComment.hashCode != mockComment.hashcode")

        jsonComment.recheckReason = ""
        assertNotEquals(jsonComment, mockComment, "jsonComment != jsonComment")

        assertThat(this, "this != comment").isNotEqualTo(comment)
    }

    @Test
    fun buildUserAgentTest() {
        val libAgent = "${GeneratedVersion.PROJECT}/${GeneratedVersion.VERSION}"
        assertEquals(akismet.buildUserAgent(), libAgent, "buildUserAgent()")

        akismet.appUserAgent = "My App/1.0"
        assertEquals(akismet.buildUserAgent(), "${akismet.appUserAgent} | $libAgent", "buildUserAgent(my app)")
    }

    @Test
    fun dateToGmtTest() {
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        val utcDate = Akismet.dateToGmt(date)
        assertEquals(Akismet.dateToGmt(localDateTime), utcDate, "dateGmt(localDateTime)")
        assertThat(comment::dateGmt).isEqualTo(utcDate)
    }

    companion object {
        private val apiKey = getKey("AKISMET_API_KEY")
        private val blog = getKey("AKISMET_BLOG")
        private val akismet = Akismet(apiKey, blog)
        private val comment = AkismetComment(
            userIp = "127.0.0.1",
            userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
        )
        private val date = Date()
        private val mockComment: AkismetComment = AkismetComment(request = getMockRequest())
        private const val REFERER = "http://www.google.com"

        init {
            with(comment) {
                referrer = REFERER
                permalink = "http://yourblogdomainname.com/blog/post=1"
                type = AkismetComment.TYPE_COMMENT
                author = "admin"
                authorEmail = "test@test.com"
                authorUrl = "http://www.CheckOutMyCoolSite.com"
                content = "It means a lot that you would take the time to review our software.  Thanks again."
                dateGmt = Akismet.dateToGmt(date)
                postModifiedGmt = dateGmt
                blogLang = "en"
                blogCharset = "UTF-8"
                userRole = AkismetComment.ADMIN_ROLE
                isTest = true
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
                userRole = comment.userRole
                recheckReason = "edit"
                isTest = true
            }
        }

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            with(akismet.logger) {
                addHandler(ConsoleHandler().apply { level = Level.FINE })
                level = Level.FINE
            }

            akismet.logger.info(comment.toString())
            akismet.logger.info(mockComment.toJson())
        }

        private fun getMockRequest(): HttpServletRequest {
            val request = Mockito.mock(HttpServletRequest::class.java)
            with(request) {
                `when`(remoteAddr).thenReturn(comment.userIp)
                `when`(requestURI).thenReturn("/blog/post=1")
                `when`(getHeader("referer")).thenReturn(REFERER)
                `when`(getHeader("Cookie")).thenReturn("name=value; name2=value2; name3=value3")
                `when`(getHeader("User-Agent")).thenReturn(comment.userAgent)
                `when`(getHeader("Accept-Encoding")).thenReturn("gzip")
                `when`(headerNames).thenReturn(
                    Collections.enumeration(listOf("User-Agent", "referer", "Cookie", "Accept-Encoding", "Null"))
                )
            }
            return request
        }
    }
}
