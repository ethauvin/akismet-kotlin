package com.example

import net.thauvin.erik.akismet.Akismet
import net.thauvin.erik.akismet.AkismetComment
import java.util.Date
import kotlin.system.exitProcess

fun main() {
    val akismet = Akismet("YOUR_API_KEY", "YOUR_BLOG_URL")
    val comment = AkismetComment(
        userIp = "127.0.0.1",
        userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6"
    )

    comment.isTest = true

    comment.referrer = "http://www.google.com"
    comment.permalink = "http://yourblogdomainname.com/blog/post=1"
    comment.type = AkismetComment.TYPE_COMMENT
    comment.author = "admin"
    comment.authorEmail = "test@test.com"
    comment.authorUrl = "http://www.CheckOutMyCoolSite.com"
    comment.dateGmt = Akismet.dateToGmt(Date())
//    comment.userRole = AkismetComment.ADMIN_ROLE
    comment.content = "It means a lot that you would take the time to review our software.  Thanks again."

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
            } else {
                System.err.println(akismet.errorMessage)
            }
        } else {
            println("The comment is not SPAM according to Akismet.")

            val hasBeenSubmitted = akismet.submitHam(comment)

            if (hasBeenSubmitted) {
                println("The comment was successfully submitted as HAM to Akismet.")
            } else {
                System.err.println(akismet.errorMessage)
            }
        }
    } else {
        System.err.println("Invalid API Key.")
        exitProcess(1)
    }

    exitProcess(0)
}
