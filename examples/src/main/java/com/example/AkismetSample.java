package com.example;

import net.thauvin.erik.akismet.Akismet;
import net.thauvin.erik.akismet.AkismetComment;

import java.util.Date;

public class AkismetSample {
    public static void main(String[] args) {
        final Akismet akismet = new Akismet("YOUR_API_KEY", "YOUR_BLOG_URL");
        final AkismetComment comment = new AkismetComment("127.0.0.1", "curl/7.29.0");

        comment.setTest(true);

        comment.setReferrer("http://www.google.com");
        comment.setPermalink("http://yourblogdomainname.com/blog/post=1");
        comment.setType(AkismetComment.TYPE_COMMENT);
        comment.setAuthor("admin");
        comment.setAuthorEmail("test@test.com");
        comment.setAuthorUrl("http://www.CheckOutMyCoolSite.com");
        comment.setDateGmt(Akismet.dateToGmt(new Date()));
        // comment.setUserRole(AkismetComment.ADMIN_ROLE);
        comment.setContent("It means a lot that you would take the time to review our software.  Thanks again.");

        // final ConsoleHandler consoleHandler = new ConsoleHandler();
        // consoleHandler.setLevel(Level.FINE);
        // final Logger logger = akismet.getLogger();
        // logger.addHandler(consoleHandler);
        // logger.setLevel(Level.FINE);

        if (akismet.verifyKey()) {
            final boolean isSpam = akismet.checkComment(comment);
            if (isSpam) {
                System.out.println("The comment is SPAM according to Akismet.");

                final boolean hasBenSubmitted = akismet.submitSpam(comment);
                if (hasBenSubmitted) {
                    System.out.println("The comment has been submitted as SPAM to Akismet");
                } else {
                    System.err.println(akismet.getErrorMessage());
                }
            } else {
                System.out.println("The comment is not SPAM according to Akismet.");

                final boolean hasBeenSubmitted = akismet.submitHam(comment);
                if (hasBeenSubmitted) {
                    System.out.println("The comment has been submitted as HAM to Akismet");
                } else {
                    System.err.println(akismet.getErrorMessage());
                }
            }
        } else {
            System.err.println("Invalid API Key.");
            System.exit(1);
        }

        System.exit(0);
    }
}
