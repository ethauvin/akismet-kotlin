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
import java.time.ZoneOffset
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
    private val blog = getKey("AKISMET_BLOG")
    private val userIp = "127.0.0.1"
    private val userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
    private val referrer = "http://www.google.com"
    private val permalink = "http://yourblogdomainname.com/blog/post=1"
    private val type = Akismet.COMMENT_TYPE_COMMENT
    private val author = "admin"
    private val authorEmail = "test@test.com"
    private val authorUrl = "http://www.CheckOutMyCoolSite.com"
    private val content = "It means a lot that you would take the time to review our software.  Thanks again."
    private val spamAuthor = "viagra-test-123"
    private val spamEmail = "akismet-guaranteed-spam@example.com"
    private val akismet = Akismet(getKey("AKISMET_API_KEY"), blog)
    private val request = Mockito.mock(HttpServletRequest::class.java)

    @BeforeClass
    fun beforeClass() {
        with(akismet.logger) {
            addHandler(ConsoleHandler().apply { level = Level.FINE })
            level = Level.FINE
        }

        Mockito.`when`(request.remoteAddr).thenReturn(userIp)
        Mockito.`when`(request.requestURI).thenReturn("/blog/post=1")
        Mockito.`when`(request.getHeader("User-Agent")).thenReturn(userAgent)
        Mockito.`when`(request.getHeader("Referer")).thenReturn(referrer)
        Mockito.`when`(request.getHeader("Cookie")).thenReturn("name=value; name2=value2; name3=value3")
        Mockito.`when`(request.getHeader("Accept-Encoding")).thenReturn("gzip")
        Mockito.`when`(request.headerNames)
            .thenReturn(Collections.enumeration(listOf("User-Agent", "Referer", "Cookie", "Accept-Encoding")))
    }

    @Test
    fun constructorsTest() {
        expectThrows(IllegalArgumentException::class.java) {
            Akismet("123456789012", "http://www.foo.com/")
            Akismet("", "http://www.foo.com/")
            Akismet("123456789012", "")
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
        assertTrue(akismet.isVerifiedKey, "isVerifiedKey -> true")
    }

    @Test
    fun checkCommentTest() {
        assertFalse(
            akismet.checkComment(
                userIp = userIp,
                userAgent = userAgent,
                referrer = referrer,
                permalink = permalink,
                type = type,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content,
                userRole = Akismet.ADMIN_ROLE,
                isTest = true), "check_comment(admin) -> false")

        akismet.isTest = true

        assertTrue(
            akismet.checkComment(
                userIp = userIp,
                userAgent = userAgent,
                referrer = referrer,
                permalink = permalink,
                type = type,
                author = spamAuthor,
                authorEmail = spamEmail,
                authorUrl = authorUrl,
                content = content), "check_comment -> true")

        assertTrue(
            akismet.checkComment(
                request,
                permalink = permalink,
                type = type,
                author = spamAuthor,
                authorEmail = spamEmail,
                authorUrl = authorUrl,
                content = content), "check_comment(request) -> true")
    }

    @Test
    fun submitHamTest() {
        assertTrue(
            akismet.submitHam(
                userIp = userIp,
                userAgent = userAgent,
                referrer = referrer,
                permalink = permalink,
                type = type,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content,
                isTest = true), "submitHam")

        assertTrue(
            akismet.submitHam(
                request,
                permalink = permalink,
                type = type,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content,
                isTest = true), "submitHam(request)")
    }

    @Test
    fun submitSpamTest() {
        assertTrue(
            akismet.submitSpam(
                userIp = userIp,
                userAgent = userAgent,
                referrer = referrer,
                permalink = permalink,
                type = type,
                author = spamAuthor,
                authorEmail = spamEmail,
                authorUrl = authorUrl,
                content = content,
                isTest = true), "submitHam")

        assertTrue(
            akismet.submitSpam(
                request,
                permalink = permalink,
                type = type,
                author = spamAuthor,
                authorEmail = spamEmail,
                authorUrl = authorUrl,
                content = content,
                isTest = true), "submitHam(request)")
    }

    @Test
    fun dateToGmtTest() {
        val date = Date()
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        assertEquals(
            akismet.dateToGmt(date),
            akismet.dateToGmt(localDateTime),
            "dateGmt(date) == dateGmt(localDateTime)")
        assertEquals(
            localDateTime.atOffset(ZoneOffset.UTC).toEpochSecond(),
            akismet.dateToGmt(date).toLong(),
            "localDateTime = dateGmt")
    }
}
