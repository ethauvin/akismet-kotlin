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
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
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

    @BeforeClass
    fun beforeClass() {
        with(akismet.logger) {
            addHandler(ConsoleHandler().apply { level = Level.FINE })
            level = Level.FINE
        }

        comment.referrer = referer
        comment.permalink = "http://yourblogdomainname.com/blog/post=1"
        comment.type = AkismetComment.TYPE_COMMENT
        comment.author = "admin"
        comment.authorEmail = "test@test.com"
        comment.authorUrl = "http://www.CheckOutMyCoolSite.com"
        comment.content = "It means a lot that you would take the time to review our software.  Thanks again."
        comment.dateGmt = akismet.dateToGmt(date)
        comment.postModifiedGmt = comment.dateGmt
        comment.blogLang = "en"
        comment.blogCharset = "UTF-8"
        comment.userRole = AkismetComment.ADMIN_ROLE
        comment.isTest = true

        akismet.logger.info(comment.toString())

        mockComment.permalink = comment.permalink
        mockComment.type = comment.type
        mockComment.authorEmail = comment.authorEmail
        mockComment.author = comment.author
        mockComment.authorUrl = comment.authorUrl
        mockComment.content = comment.content
        mockComment.dateGmt = comment.dateGmt
        mockComment.postModifiedGmt = comment.dateGmt
        mockComment.blogLang = comment.blogLang
        mockComment.blogCharset = comment.blogCharset
        mockComment.userRole = comment.userRole
        mockComment.recheckReason = "edit"
        mockComment.isTest = true

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
        assertFalse(akismet.isVerifiedKey, "isVerifiedKey -> false")
        assertTrue(akismet.verifyKey(), "verifyKey()")
        assertEquals(akismet.response, "valid", "response -> valid")
        assertTrue(akismet.isVerifiedKey, "isVerifiedKey -> true")

        assertFalse(Akismet("123456789012").verifyKey(), "verifyKey() --> false")
    }

    @Test
    fun checkMockComment() {
        assertEquals(mockComment.userIp, comment.userIp, "userIp")
        assertEquals(mockComment.userAgent, comment.userAgent, "userAgent")
        assertEquals(mockComment.referrer, comment.referrer, "referrer")
        assertTrue(mockComment.serverEnv.containsKey("HTTP_ACCEPT_ENCODING"), "HTTP_ACCEPT_ENCODING")
        assertTrue(mockComment.serverEnv.containsKey("REQUEST_URI"), "REQUEST_URI")
    }

    @Test
    fun emptyCommentTest() {
        expectThrows(IllegalArgumentException::class.java) {
            akismet.checkComment(AkismetComment("", ""))
        }
    }

    @Test
    fun resetTest() {
        akismet.reset()
        assertTrue(
            akismet.debugHelp == "" && akismet.httpStatusCode == 0 && !akismet.isDiscard &&
                !akismet.isVerifiedKey && akismet.proTip == "" && akismet.response == ""
        )
    }

    @Test
    fun checkCommentTest() {
        assertFalse(akismet.checkComment(comment), "check_comment(admin) -> false")
        assertEquals(akismet.response, "false", "response -> false")
        comment.userRole = ""
        assertTrue(akismet.checkComment(comment), "check_comment -> true")
        assertEquals(akismet.response, "true", "response -> true")

        assertFalse(akismet.checkComment(mockComment), "check_comment(request) -> false")
        mockComment.userRole = ""
        assertTrue(akismet.checkComment(mockComment), "check_comment(request) -> true")

        assertEquals(akismet.httpStatusCode, 200, "status code")
    }

    @Test
    fun executeMethodTest() {
        akismet.executeMethod(
            "https://$apiKey.rest.akismet.com/1.1/comment-check".toHttpUrlOrNull(),
            FormBody.Builder().apply { add("is_test", "1") }.build()
        )
        assertTrue(akismet.debugHelp.isNotEmpty(), "debugHelp not empty")
    }

    @Test
    fun submitHamTest() {
        assertTrue(akismet.submitHam(comment), "submitHam")

        assertTrue(akismet.submitHam(mockComment), "submitHam(request)")
    }

    @Test
    fun submitSpamTest() {
        assertTrue(akismet.submitSpam(comment), "submitHam")
        assertTrue(akismet.submitSpam(mockComment), "submitHam(request)")
    }

    @Test
    fun dateToGmtTest() {
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        val utcDate = akismet.dateToGmt(date)
        assertEquals(akismet.dateToGmt(localDateTime), utcDate, "dateGmt(localDateTime) = utcDate")
        assertEquals(comment.dateGmt, utcDate, "dateGmt == utcDate")
    }

    private fun getMockRequest(): HttpServletRequest {
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.remoteAddr).thenReturn(comment.userIp)
        Mockito.`when`(request.requestURI).thenReturn("/blog/post=1")
        Mockito.`when`(request.getHeader("User-Agent")).thenReturn(comment.userAgent)
        Mockito.`when`(request.getHeader("referer")).thenReturn(referer)
        Mockito.`when`(request.getHeader("Cookie")).thenReturn("name=value; name2=value2; name3=value3")
        Mockito.`when`(request.getHeader("Accept-Encoding")).thenReturn("gzip")
        Mockito.`when`(request.headerNames)
            .thenReturn(Collections.enumeration(listOf("User-Agent", "referer", "Cookie", "Accept-Encoding")))
        return request
    }
}
