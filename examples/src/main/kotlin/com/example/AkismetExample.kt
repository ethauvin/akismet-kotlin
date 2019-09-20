package com.example

import net.thauvin.erik.akismet.Akismet
import net.thauvin.erik.akismet.AkismetComment
import kotlin.system.exitProcess

fun main() {
    val akismet = Akismet("YOUR_API_KEY", "YOUR_BLOG_URL")
    val comment = AkismetComment(
        userIp = "127.0.0.1",
        userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6",
        referrer = "http://www.google.com",
        permalink = "http://yourblogdomainname.com/blog/post=1",
        type = AkismetComment.TYPE_COMMENT,
        author = "admin",
        authorEmail = "test@test.com",
        authorUrl = "http://www.CheckOutMyCoolSite.com",
//        userRole = AkismetComment.ADMIN_ROLE,
        content = "It means a lot that you would take the time to review our software.  Thanks again.",
        isTest = true)

//    with(akismet.logger) {
//        addHandler(ConsoleHandler().apply { level = Level.FINE })
//        level = Level.FINE
//    }

    if (akismet.verifyKey()) {
        val isSpam = akismet.checkComment(comment)
        if (isSpam) {
            println("The comment is SPAM according to Akismet.")

            val hasBeenSubmitted = akismet.submitSpam(comment)

            if (hasBeenSubmitted) {
                println("The comment was successfully submitted as SPAM to Akismet.")
            }
        } else {
            println("The comment is not SPAM (HAM) according to Akismet.")

            val hasBeenSubmitted = akismet.submitHam(comment)

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
