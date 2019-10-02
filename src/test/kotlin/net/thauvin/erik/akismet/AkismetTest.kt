/*
 * AkismetTest.kt
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

import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertNotEquals
import org.testng.Assert.assertTrue
import org.testng.Assert.expectThrows
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Collections
import java.util.Date
import java.util.Properties
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import javax.servlet.http.HttpServletRequest

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
    private val apiKey = getKey("AKISMET_API_KEY")
    private val blog = getKey("AKISMET_BLOG")
    private val referer = "http://www.google.com"
    private val date = Date()
    private val comment = AkismetComment(
        userIp = "127.0.0.1",
        userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
    )
    private val akismet = Akismet(apiKey, blog)
    private val mockComment: AkismetComment = AkismetComment(request = getMockRequest())
    private val emptyFormBody = FormBody.Builder().build()

    @BeforeClass
    fun beforeClass() {
        with(akismet.logger) {
            addHandler(ConsoleHandler().apply { level = Level.FINE })
            level = Level.FINE
        }

        with(comment) {
            referrer = referer
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

        akismet.logger.info(comment.toString())

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

        akismet.logger.info(mockComment.toString())
    }

    @Test
    fun constructorsTest() {
        expectThrows(IllegalArgumentException::class.java) {
            Akismet("")
        }
        expectThrows(IllegalArgumentException::class.java) {
            Akismet("1234")
        }
        expectThrows(IllegalArgumentException::class.java) {
            Akismet("123456789 12")
        }
        expectThrows(IllegalArgumentException::class.java) {
            Akismet("123456789012", "")
        }
        expectThrows(IllegalArgumentException::class.java) {
            Akismet("1234", "foo")
        }
    }

    @Test
    fun blogPropertyTest() {
        expectThrows(IllegalArgumentException::class.java) {
            akismet.blog = ""
        }

        assertEquals(akismet.blog, blog, "valid property")
    }

    @Test
    fun verifyKeyTest() {
        with(akismet) {
            assertFalse(isVerifiedKey, "isVerifiedKey -> false")

            assertTrue(verifyKey(), "verifyKey()")
            assertEquals(response, "valid", "response -> valid")
            assertTrue(isVerifiedKey, "isVerifiedKey -> true")

            reset()
            assertTrue(!isVerifiedKey && response.isEmpty() && httpStatusCode == 0, " reset")
        }

        assertFalse(Akismet("123456789012").verifyKey(), "verifyKey() --> false")
    }

    @Test
    fun mockCommentTest() {
        with(mockComment) {
            assertEquals(userIp, comment.userIp, "userIp")
            assertEquals(userAgent, comment.userAgent, "userAgent")
            assertEquals(referrer, comment.referrer, "referrer")
            assertEquals(serverEnv["HTTP_ACCEPT_ENCODING"], "gzip", "HTTP_ACCEPT_ENCODING")
            assertTrue(serverEnv.containsKey("REQUEST_URI"), "REQUEST_URI")
            assertEquals(serverEnv["REMOTE_ADDR"], comment.userIp, "REMOTE_ADDR")
            assertTrue(serverEnv["HTTP_NULL"].toString().isEmpty(), "HTTP_NULL")
            assertFalse(serverEnv.containsKey("HTTP_COOKIE"), "HTTP_COOKIE")
            assertEquals(serverEnv.size, 6, "serverEnv size")
        }
    }

    @Test
    fun emptyCommentTest() {
        expectThrows(IllegalArgumentException::class.java) {
            akismet.checkComment(AkismetComment("", ""))
        }

        val empty = AkismetComment("", "")
        with(empty) {
            assertFalse(isTest, "isTest")
            assertEquals(referrer, "", "referrer")
            assertEquals(permalink, "", "permalink")
            assertEquals(type, "", "type")
            assertEquals(authorEmail, "", "authorEmail")
            assertEquals(author, "", "author")
            assertEquals(authorUrl, "", "authorUrl")
            assertEquals(content, "", "content")
            assertEquals(dateGmt, "", "dateGmt")
            assertEquals(postModifiedGmt, "", "postModifiedGmt")
            assertEquals(blogLang, "", "blogLang")
            assertEquals(blogCharset, "", "blogCharset")
            assertEquals(userRole, "", "userRole")
            assertEquals(recheckReason, "", "recheckReason")
            assertEquals(serverEnv.size, 0, "serverEnv size")

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

                assertEquals(referrer, expected, "referrer: [$s]")
                assertEquals(permalink, expected, "permalink: [$s]")
                assertEquals(type, expected, "type: [$s]")
                assertEquals(authorEmail, expected, "authorEmail: [$s]")
                assertEquals(author, expected, "author: [$s]")
                assertEquals(authorUrl, expected, "authorUrl: [$s]")
                assertEquals(content, expected, "content: [$s]")
                assertEquals(dateGmt, expected, "dateGmt: [$s]")
                assertEquals(postModifiedGmt, expected, "postModifiedGmt: [$s]")
                assertEquals(blogLang, expected, "blogLang: [$s]")
                assertEquals(blogCharset, expected, "blogCharset: [$s]")
                assertEquals(userRole, expected, "userRole: [$s]")
                assertEquals(recheckReason, expected, "recheckReason: [$s]")
                assertEquals(serverEnv.size, 0, "serverEnv size: [$s]")
            }
        }
    }

    @Test
    fun emptyResponseTest() {
        with(akismet) {
            assertTrue(
                executeMethod(
                    "https://postman-echo.com/status/200".toHttpUrlOrNull(), emptyFormBody, true
                )
            )
            val expected = "{\"status\":200}"
            assertEquals(response, expected, expected)
            assertTrue(errorMessage.contains(expected), "errorMessage contains $expected")

            reset()
            assertTrue(httpStatusCode == 0 && errorMessage.isEmpty(), "reset")
        }
    }

    @Test
    fun proTipResponseTest() {
        with(akismet) {
            assertFalse(
                executeMethod(
                    "https://postman-echo.com/response-headers?x-akismet-pro-tip=discard".toHttpUrlOrNull(),
                    emptyFormBody
                )
            )
            assertEquals(proTip, "discard")
            assertTrue(isDiscard, "isDiscard")

            reset()
            assertTrue(!isDiscard && response.isEmpty() && httpStatusCode == 0)
        }
    }

    @Test
    fun checkCommentTest() {
        with(akismet) {
            assertFalse(checkComment(comment), "check_comment(admin) -> false")
            assertEquals(response, "false", "response -> false")
            comment.userRole = ""
            assertTrue(checkComment(comment), "check_comment -> true")
            assertEquals(response, "true", "response -> true")

            assertFalse(checkComment(mockComment), "check_comment(mock) -> false")
            assertEquals(response, "false", "mock response -> false")
            mockComment.userRole = ""
            assertTrue(checkComment(mockComment), "check_comment(mock) -> true")
            assertEquals(response, "true", "mock response -> true")

            assertEquals(httpStatusCode, 200, "status code")
        }
    }

    @Test
    fun executeMethodTest() {
        with(akismet) {
            executeMethod(
                "https://$apiKey.rest.akismet.com/1.1/comment-check".toHttpUrlOrNull(),
                FormBody.Builder().apply { add("is_test", "1") }.build()
            )
            assertTrue(debugHelp.isNotEmpty(), "debugHelp not empty")

            reset()
            assertTrue(httpStatusCode == 0 && debugHelp.isEmpty() && response.isEmpty(), "reset")
        }
    }

    @Test
    fun invalidApiTest() {
        akismet.executeMethod("https://.com".toHttpUrlOrNull(), emptyFormBody)
        assertTrue(akismet.errorMessage.startsWith("Invalid API"))
    }

    @Test
    fun ioErrorTest() {
        akismet.executeMethod("https://www.doesnotexists.com".toHttpUrlOrNull(), emptyFormBody)
        assertTrue(akismet.errorMessage.contains("IO error"))
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
        val jsonComment = Akismet.jsonComment(mockComment.toString())

        assertEquals(jsonComment, mockComment, "equals")
        assertEquals(jsonComment.hashCode(), mockComment.hashCode(), "hashcode")

        assertNotEquals(jsonComment, comment, "json is different")
        assertNotEquals(jsonComment.hashCode(), comment.hashCode(), "json hashcode is different")

        jsonComment.recheckReason = ""
        assertNotEquals(jsonComment, mockComment, "not equals on change")

        assertNotEquals(this, comment, "wrong object")
    }

    @Test
    fun buildUserAgentTest() {
        val libAgent = "${GeneratedVersion.PROJECT}/${GeneratedVersion.VERSION}"
        assertEquals(akismet.buildUserAgent(), libAgent, "libAgent")

        akismet.appUserAgent = "My App/1.0"
        assertEquals(
            akismet.buildUserAgent(), "${akismet.appUserAgent} | $libAgent",
            "my app"
        )
    }

    @Test
    fun dateToGmtTest() {
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        val utcDate = Akismet.dateToGmt(date)
        assertEquals(Akismet.dateToGmt(localDateTime), utcDate, "dateGmt(localDateTime) = utcDate")
        assertEquals(comment.dateGmt, utcDate, "dateGmt == utcDate")
    }

    private fun getMockRequest(): HttpServletRequest {
        val request = Mockito.mock(HttpServletRequest::class.java)
        with(request) {
            `when`(remoteAddr).thenReturn(comment.userIp)
            `when`(requestURI).thenReturn("/blog/post=1")
            `when`(getHeader("referer")).thenReturn(referer)
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
