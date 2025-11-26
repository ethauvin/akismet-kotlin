/*
 * CommentType.kt
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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer that encodes the enum as its string value and decodes unknown values to NONE.
 */
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
object CommentTypeSerializer : KSerializer<CommentType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("net.thauvin.erik.akismet.CommentType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CommentType) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): CommentType {
        val str = decoder.decodeString()
        return CommentType.fromValue(str)
    }
}

/**
 * Defines the comment types.
 *
 * Uses a custom serializer so unknown string values deserialize to NONE instead
 * of throwing an exception.
 */
@Serializable(with = CommentTypeSerializer::class)
enum class CommentType(val value: String) {
    /**
     * A blog comment.
     */
    COMMENT("comment"),

    /**
     * A top-level forum post.
     */
    FORUM_POST("forum-post"),

    /**
     * A reply to a top-level forum post.
     */
    REPLY("reply"),

    /**
     * A blog post.
     */
    BLOG_POST("blog-post"),

    /**
     * A contact form or feedback form submission.
     */
    CONTACT_FORM("contact-form"),

    /**
     * A new user account.
     */
    SIGNUP("signup"),

    /**
     * A message sent between just a few users.
     */
    MESSAGE("message"),

    /**
     * A pingback.
     */
    PINGBACK("pingback"),

    /**
     * A trackback.
     */
    TRACKBACK("trackback"),

    /**
     * A Twitter message.
     */
    TWEET("tweet"),

    /**
     * Undefined / none.
     */
    NONE("");

    companion object {
        fun fromValue(value: String?): CommentType =
            if (value == null) NONE else CommentType.entries.firstOrNull { it.value == value } ?: NONE
    }
}
