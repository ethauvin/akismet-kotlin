package com.example;

import net.thauvin.erik.akismet.Akismet;

public class AkismetSample {
    public static void main(String[] args) {
        final Akismet akismet = new Akismet("YOUR_API_KEY", "YOUR_BLOG_URL");

        final String userIp = "127.0.0.1";
        final String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6";
        final String author = "admin";
        final String authorEmail = "test@test.com";
        final String authorUrl = "http://www.CheckOutMyCoolSite.com";
        final String content = "It means a lot that you would take the time to review our software.  Thanks again.";

        akismet.setTest(true);

         // final ConsoleHandler consoleHandler = new ConsoleHandler();
         // consoleHandler.setLevel(Level.FINE);
         // final Logger logger = akismet.getLogger();
         // logger.addHandler(consoleHandler);
         // logger.setLevel(Level.FINE);

        if (akismet.verifyKey()) {
            final boolean isSpam = akismet.checkComment(userIp,
                                                        userAgent,
                                                        "",
                                                        "",
                                                        Akismet.COMMENT_TYPE_COMMENT,
                                                        author,
                                                        authorEmail,
                                                        authorUrl,
                                                        content);
            if (isSpam) {
                System.out.println("The comment is SPAM according to Akismet.");

                final boolean hasBenSubmitted = akismet.submitSpam(userIp,
                                                                  userAgent,
                                                                  "",
                                                                  "",
                                                                  Akismet.COMMENT_TYPE_COMMENT,
                                                                  author,
                                                                  authorEmail,
                                                                  authorUrl,
                                                                  content);
                if (hasBenSubmitted) {
                    System.out.println("The comment has been submitted as SPAM to Akismet");
                }
            } else {
                System.out.println("The comment is not SPAM (HAM) according to Akismet.");

                final boolean hasBeenSubmitted = akismet.submitHam(userIp,
                                                                 userAgent,
                                                                 "",
                                                                 "",
                                                                 Akismet.COMMENT_TYPE_COMMENT,
                                                                 author,
                                                                 authorEmail,
                                                                 authorUrl,
                                                                 content);
                if (hasBeenSubmitted) {
                    System.out.println("The comment has been submitted as HAM to Akismet");
                }
            }
        } else {
            System.err.println("Invalid API Key.");
            System.exit(1);
        }

        System.exit(0);
    }
}
