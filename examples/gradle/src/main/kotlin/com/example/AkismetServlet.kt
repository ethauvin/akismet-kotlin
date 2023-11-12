package com.example

import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.thauvin.erik.akismet.Akismet
import net.thauvin.erik.akismet.AkismetComment
import java.io.IOException
import java.util.Date

@WebServlet(description = "Akismet Servlet", displayName = "Akismet", urlPatterns = ["/comment/*"])
class AkismetServlet : HttpServlet() {
    private val akismet = Akismet("YOUR_API_KEY", "http://yourblogdomainname.com/blog/")

    @Throws(ServletException::class, IOException::class)
    public override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        val id = request.getParameter("id")

        akismet.appUserAgent = request.servletContext.serverInfo

        val comment = AkismetComment(request)
        with(comment) {
            permalink = "${akismet.blog}/comment/$id"
            type = AkismetComment.TYPE_COMMENT
            author = request.getParameter("name")
            authorEmail = request.getParameter("email")
            dateGmt = Akismet.dateToGmt(Date())
            content = request.getParameter("comment")
        }

        val isSpam = akismet.checkComment(comment)

        saveComment(
            id = id,
            name = comment.author,
            email = comment.authorEmail,
            date = comment.dateGmt,
            comment = comment.content,
            json = comment.toJson(),
            isSpam = isSpam
        )

        // ...
    }

    @Suppress("UNUSED_PARAMETER")
    private fun saveComment(
        id: String,
        name: String?,
        email: String?,
        date: String?,
        comment: String?,
        json: String,
        isSpam: Boolean
    ) {
        // ...
    }
}
