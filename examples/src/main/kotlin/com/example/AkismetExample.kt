package com.example

import net.thauvin.erik.akismet.Akismet
import net.thauvin.erik.akismet.AkismetComment
import java.util.Date
import kotlin.system.exitProcess

fun main() {
    val akismet = Akismet(apiKey = "YOUR_API_KEY", blog = "https://yourblogdomainname.com/blog/")
    val comment = AkismetComment(userIp = "127.0.0.1", userAgent = "curl/7.29.0")

    with(comment) {
        isTest = true

        referrer = "https://www.google.com"
        permalink = "${akismet.blog}post=1"
        type = AkismetComment.TYPE_COMMENT
        author = "admin"
        authorEmail = "test@test.com"
        authorUrl = "https://www.CheckOutMyCoolSite.com"
        dateGmt = Akismet.dateToGmt(Date())
//        userRole = AkismetComment.ADMIN_ROLE
        content = "It means a lot that you would take the time to review our software. Thanks again."
    }

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
