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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import net.thauvin.erik.akismet.Akismet.Companion.jsonComment
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.text.contains

/**
 * [Akismet] Tests
 *
 * `AKISMET_API_KEY` and `AKISMET_BLOG` should be set in env vars or `local.properties`
 */
@ExtendWith(BeforeAllTests::class)
class AkismetTests {
    private val emptyFormBody = FormBody.Builder().build()

    companion object {
        private val apiKey = TestUtils.getKey("AKISMET_API_KEY")
        private val blog = TestUtils.getKey("AKISMET_BLOG")
    }

    @Nested
    @DisplayName("Constructor Tests")
    inner class ConstructorTests {
        @Test
        fun `Constructor with API key arg empty`() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("")
            }
        }

        @Test
        fun `Constructor with API key arg too long`() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("123456789 12")
            }
        }

        @Test
        fun `Constructor with API key arg too short`() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("1234")
            }
        }

        @Test
        fun `Constructor with empty blog arg`() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("123456789012", "")
            }
        }

        @Test
        fun `Constructor with invalid key and blog args`() {
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                Akismet("1234", "foo")
            }
        }
    }

    @Nested
    @DisplayName("Date Conversion Tests")
    inner class DateConversionTests {
        val sampleDate: ZonedDateTime = LocalDateTime.of(1997, 8, 29, 2, 0, 0)
            .atZone(ZoneId.of("America/New_York"))

        @Test
        fun `Date should convert correctly to GMT string`() {
            val date = Date.from(sampleDate.toInstant())
            val result = Akismet.dateToGmt(date)
            assertEquals("1997-08-28T23:00:00-07:00", result)
        }

        @Test
        fun `LocalDateTime should convert correctly to GMT string`() {
            val result = Akismet.dateToGmt(sampleDate.toLocalDateTime())
            assertEquals("1997-08-29T02:00:00-07:00", result)
        }
    }

    @Nested
    @DisplayName("JSON Deserialization Tests")
    inner class JsonDeserializationTests {
        @Test
        fun `Validate JSON deserialization`() {
            val config = CommentConfig.Builder("127.0.0.1", "Mozilla/5.0")
                .referrer("https://example.com")
                .type(CommentType.COMMENT)
                .author("John Doe")
                .authorEmail("john.doe@example.com")
                .authorUrl("https://johndoe.com")
                .content("This is a comment")
                .dateGmt("2023-10-20T10:20:30Z")
                .postModifiedGmt("2023-10-20T11:00:00Z")
                .blogLang("en")
                .blogCharset("UTF-8")
                .userRole("administrator")
                .isTest(true)
                .recheckReason("Check the spam detection")
                .serverEnv(mapOf("key1" to "value1", "key2" to "value2"))
                .build()
            val validJson = AkismetComment(config).toJson();

            val comment = jsonComment(validJson)

            Assertions.assertEquals("127.0.0.1", comment.userIp)
            Assertions.assertEquals("Mozilla/5.0", comment.userAgent)
            Assertions.assertEquals("https://example.com", comment.referrer)
            Assertions.assertEquals("comment", comment.type.value)
            Assertions.assertEquals("John Doe", comment.author)
            Assertions.assertEquals("john.doe@example.com", comment.authorEmail)
            Assertions.assertEquals("https://johndoe.com", comment.authorUrl)
            Assertions.assertEquals("This is a comment", comment.content)
            Assertions.assertEquals("2023-10-20T10:20:30Z", comment.dateGmt)
            Assertions.assertEquals("2023-10-20T11:00:00Z", comment.postModifiedGmt)
            Assertions.assertEquals("en", comment.blogLang)
            Assertions.assertEquals("UTF-8", comment.blogCharset)
            Assertions.assertEquals("administrator", comment.userRole)
            Assertions.assertTrue(comment.isTest)
            Assertions.assertEquals("Check the spam detection", comment.recheckReason)
            Assertions.assertEquals(mapOf("key1" to "value1", "key2" to "value2"), comment.serverEnv)
        }

        @Test
        fun `Invalid JSON deserialization`() {
            val invalidJson = """
            {
                "userIp": "127.0.0.1",
                "userAgent": "Mozilla/5.0"
                // Missing closing brace
        """.trimIndent()

            val exception = Assertions.assertThrows(SerializationException::class.java) {
                jsonComment(invalidJson)
            }

            Assertions.assertTrue(exception.message?.contains("Unexpected JSON token") == true)
        }

        @Test
        @OptIn(ExperimentalSerializationApi::class)
        fun `Empty JSON deserialization`() {
            val emptyJson = "{}"

            Assertions.assertThrows(MissingFieldException::class.java) {
                jsonComment(emptyJson)
            }
        }

        @Test
        @OptIn(ExperimentalSerializationApi::class)
        fun `JSON deserialization with missing mandatory fields`() {
            val partialJson = """
            {
                "userIp": "127.0.0.1"
            }
        """.trimIndent()

            Assertions.assertThrows(MissingFieldException::class.java) {
                jsonComment(partialJson)
            }
        }

        @Test
        fun `JSON deserialization with unexpected fields`() {
            val extraFieldJson = """
            {
                "userIp": "127.0.0.1",
                "userAgent": "Mozilla/5.0",
                "extraField": "unexpected"
            }
        """.trimIndent()

            val comment = jsonComment(extraFieldJson)

            Assertions.assertEquals("127.0.0.1", comment.userIp)
            Assertions.assertEquals("Mozilla/5.0", comment.userAgent)
        }
    }

    @Nested
    @DisplayName("Response Tests")
    inner class ResponseTests {
        @Test
        fun `Handle blank response`() {
            val akismet = Akismet(apiKey)
            assertTrue(
                akismet.executeMethod(
                    "https://erik.thauvin.net/blank.html".toHttpUrl(), emptyFormBody, true
                )
            )
            val expected = ""
            assertThat(akismet, "executeMethod(blank)").all {
                prop(Akismet::response).isEqualTo(expected)
                prop(Akismet::errorMessage).contains("blank")
            }
        }

        @Test
        fun `Handle debug help header`() {
            val akismet = Akismet(apiKey)
            akismet.executeMethod(
                "https://$apiKey.rest.akismet.com/1.1/comment-check".toHttpUrl(),
                FormBody.Builder().apply { add("is_test", "1") }.build()
            )

            assertThat(akismet, "x-akismet-debug-help").all {
                prop(Akismet::httpStatusCode).isEqualTo(200)
                prop(Akismet::debugHelp).isEqualTo("Empty \"blog\" value")
            }

            akismet.reset()
            assertThat(akismet, "akismet.reset()").all {
                prop(Akismet::httpStatusCode).isEqualTo(0)
                prop(Akismet::debugHelp).isEmpty()
                prop(Akismet::response).isEmpty()
            }
        }

        @Test
        fun `Handle invalid response`() {
            val akismet = Akismet(apiKey)
            assertTrue(
                akismet.executeMethod(
                    "https://postman-echo.com/status/200".toHttpUrl(), emptyFormBody, true
                )
            )
            val expected = "{\"status\":200}"
            assertThat(akismet, "executeMethod(200)").all {
                prop(Akismet::response).isEqualTo(expected)
                prop(Akismet::errorMessage).contains(expected)
            }

            akismet.reset()
            assertThat(akismet, "akismet.reset()").all {
                prop(Akismet::httpStatusCode).isEqualTo(0)
                prop(Akismet::errorMessage).isEmpty()
            }
        }

        @Test
        fun `Handle IO error`() {
            val akismet = Akismet(apiKey)
            akismet.executeMethod("https://www.foobarxyz.com".toHttpUrl(), emptyFormBody)
            assertThat(akismet).prop(Akismet::errorMessage).contains("IO error")
        }

        @Test
        fun `Handle invalid API URL`() {
            val akismet = Akismet(apiKey)
            assertThrows(
                java.lang.IllegalArgumentException::class.java
            ) { akismet.executeMethod("https://.com".toHttpUrl(), emptyFormBody) }
        }

        @Test
        fun `Handle pro tip header`() {
            val akismet = Akismet(apiKey)
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


        @Nested
        @DisplayName("Validation Tests")
        inner class ValidationTests {
            @Test
            fun `Validate api key`() {
                val akismet = Akismet(apiKey, blog)
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

            @Test
            fun `Validate blog property`() {
                val akismet = Akismet(apiKey, blog)
                assertThrows(IllegalArgumentException::class.java) {
                    akismet.blog = ""
                }

                assertThat(akismet).prop(Akismet::blog).isEqualTo(blog)
            }


            @Nested
            @DisplayName("User Agent Validation Tests")
            inner class UserAgentValidationTests {
                val libAgent = "${GeneratedVersion.PROJECT}/${GeneratedVersion.VERSION}"

                @Test
                fun `Validate custom user agent`() {
                    val akismet = Akismet(apiKey)
                    akismet.appUserAgent = "My App/1.0"

                    assertEquals(
                        "${akismet.appUserAgent} | $libAgent",
                        akismet.buildUserAgent(),
                        "buildUserAgent(My App/1.0)"
                    )
                }

                @Test
                fun `Validate default user agent`() {
                    val akismet = Akismet(apiKey)
                    assertEquals(
                        libAgent, akismet.buildUserAgent(),
                        "buildUserAgent($libAgent)"
                    )
                }
            }
        }
    }
}
