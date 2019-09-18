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
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.Assert.expectThrows
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.io.FileInputStream
import java.util.Collections
import java.util.Properties
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import javax.servlet.http.HttpServletRequest

/**
 * The <code>AkismetTest</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-09-17
 * @since 1.0
 */

fun getApiKey(): String {
    var apiKey = System.getenv("AKISMET_API_KEY") ?: ""
    if (apiKey.isBlank()) {
        val localProps = File("local.properties")
        if (localProps.exists())
            localProps.apply {
                if (exists()) {
                    FileInputStream(this).use { fis ->
                        Properties().apply {
                            load(fis)
                            apiKey = getProperty("AKISMET_API_KEY", "")
                        }
                    }
                }
            }
    }
    return apiKey
}

class AkismetTest {
    private val userIp = "127.0.0.1"
    private val userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
    private val referrer = "http://www.google.com"
    private val permalink = "http://yourblogdomainname.com/blog/post=1"
    private val type = "comment"
    private val author = "admin"
    private val authorEmail = "test@test.com"
    private val authorUrl = "http://www.CheckOutMyCoolSite.com"
    private val content = "It means a lot that you would take the time to review our software.  Thanks again."
    private val akismet = Akismet(getApiKey(), "http://erik.thauvin.net/blog/")
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
    fun constructorTest() {
        expectThrows(IllegalArgumentException::class.java) {
            Akismet("123456789012", "http://www.foo.com/")
            Akismet("", "http://www.foo.com/")
            Akismet("123456789012", "")
        }
    }

    @Test
    fun verifyKeyTest() {
        assertFalse(akismet.isValidKey, "isValidKey -> false")
        assertTrue(akismet.verifyKey(), "verify_key")
        assertTrue(akismet.isValidKey, "isValidKey -> true")
    }

    @Test
    fun checkCommentTest() {
//        assertFalse(akismet.checkComment(userIp = userIp,
//                                         userAgent = userAgent,
//                                         referrer = referrer,
//                                         permalink = permalink,
//                                         type = type,
//                                         author = author,
//                                         authorEmail = authorEmail,
//                                         authorUrl = authorUrl,
//                                         content = content,
//                                         userRole = Akismet.ADMIN_ROLE,
//                                         isTest = true), "check_comment -> false")
//
//        assertTrue(akismet.checkComment(userIp = userIp,
//                                        userAgent = userAgent,
//                                        referrer = referrer,
//                                        permalink = permalink,
//                                        type = type,
//                                        author = author,
//                                        authorEmail = authorEmail,
//                                        authorUrl = authorUrl,
//                                        content = content,
//                                        isTest = true), "check_comment -> true")

        assertTrue(akismet.checkComment(request,
                                        permalink = permalink,
                                        type = type,
                                        author = author,
                                        authorEmail = authorEmail,
                                        authorUrl = authorUrl,
                                        content = content,
                                        isTest = true), "check_comment(request) -> true")
    }
}