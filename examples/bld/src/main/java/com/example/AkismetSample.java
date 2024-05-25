package com.example;

import net.thauvin.erik.akismet.Akismet;
import net.thauvin.erik.akismet.AkismetComment;
import net.thauvin.erik.akismet.CommentConfig;
import net.thauvin.erik.akismet.CommentType;

import java.util.Date;

public class AkismetSample {
    public static void main(String... args) {
        if (args.length == 1 && !args[0].isBlank()) {
            final Akismet akismet = new Akismet(args[0], "https://yourblogdomainname.com/blog/");
            final AkismetComment comment = new AkismetComment(
                    new CommentConfig.Builder("127.0.0.1", "curl/7.29.0")
                            .isTest(true)
                            .referrer("https://www.google.com")
                            .permalink(akismet.getBlog() + "post=1")
                            .type(CommentType.COMMENT)
                            .author("admin")
                            .authorEmail("test@test.com")
                            .authorUrl("http://www.CheckOutMyCoolSite.com")
                            .dateGmt(Akismet.dateToGmt(new Date()))
//                            .userRole(AkismetComment.ADMIN_ROLE)
                            .content("It means a lot that you would take the time to review our software. Thanks again.")
                            .build()
            );

//             final ConsoleHandler consoleHandler = new ConsoleHandler();
//             consoleHandler.setLevel(Level.FINE);
//             final Logger logger = akismet.getLogger();
//             logger.addHandler(consoleHandler);
//             logger.setLevel(Level.FINE);

            if (akismet.verifyKey()) {
                final boolean isSpam = akismet.checkComment(comment);
                if (isSpam) {
                    System.out.println("The comment is SPAM according to Akismet.");

                    final boolean hasBeenSubmitted = akismet.submitSpam(comment);
                    if (hasBeenSubmitted) {
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
        } else {
            System.err.println("Please specify an API key.");
            System.exit(1);
        }
    }
}
