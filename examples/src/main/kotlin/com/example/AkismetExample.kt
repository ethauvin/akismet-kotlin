package com.example

import net.thauvin.erik.akismet.Akismet
import kotlin.system.exitProcess

fun main() {
    val akismet = Akismet("YOUR_API_KEY", "YOUR_BLOG_URL")

    val userIp = "127.0.0.1"
    val userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
    val author = "admin"
    val authorEmail = "test@test.com"
    val authorUrl = "http://www.CheckOutMyCoolSite.com"
    val content = "It means a lot that you would take the time to review our software.  Thanks again."

    akismet.isTest = true

//    with(akismet.logger) {
//        addHandler(ConsoleHandler().apply { level = Level.FINE })
//        level = Level.FINE
//    }

    if (akismet.verifyKey()) {
        val isSpam = akismet.checkComment(
            userIp = userIp,
            userAgent = userAgent,
            type = Akismet.COMMENT_TYPE_COMMENT,
            author = author,
            authorEmail = authorEmail,
            authorUrl = authorUrl,
            userRole = Akismet.ADMIN_ROLE,
            content = content)
        if (isSpam) {
            println("The comment is SPAM according to Akismet.")

            val hasBeenSubmitted = akismet.submitSpam(
                userIp = userIp,
                userAgent = userAgent,
                type = Akismet.COMMENT_TYPE_COMMENT,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content)

            if (hasBeenSubmitted) {
                println("The comment was successfully submitted as SPAM to Akismet.")
            }
        } else {
            println("The comment is not SPAM (HAM) according to Akismet.")

            val hasBeenSubmitted = akismet.submitHam(
                userIp = userIp,
                userAgent = userAgent,
                type = Akismet.COMMENT_TYPE_COMMENT,
                author = author,
                authorEmail = authorEmail,
                authorUrl = authorUrl,
                content = content)

            if (hasBeenSubmitted) {
                println("The comment was successfully submitted as HAM to Akismet.")
            }
        }
    } else {
        System.err.println("Invalid API Key.")
        exitProcess(1)
    }

    exitProcess(0)
}
