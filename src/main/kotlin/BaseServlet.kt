package kws

import jakarta.servlet.ServletConfig
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.html.HTML
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import java.sql.Connection

abstract class BaseServlet : HttpServlet() {

    protected val applicationContext: ApplicationContext by lazy {
        servletContext
            .getAttribute(ApplicationContext.KEY_NAME) as ApplicationContext
    }

    override fun init(config: ServletConfig) {
        super.init(config)
    }

    protected fun <T> withConnection(block: (Connection) -> T): T =
        applicationContext.lookup<Connection>().let(block)

    protected fun html(block: HTML.() -> Unit): String =
        createHTML().html { block(this) }

    protected fun HttpServletRequest.httpMethod(): String =
        (getHeader("X-HTTP-Method-Override") ?: method).uppercase()

    protected fun HttpServletResponse.notFound(message: String) {
        error(HttpServletResponse.SC_NOT_FOUND, message)
    }

    protected fun HttpServletResponse.unauthorized(message: String) {
        error(HttpServletResponse.SC_UNAUTHORIZED, message)
    }

    protected fun HttpServletResponse.methodNotAllowed(message: String) {
        error(HttpServletResponse.SC_METHOD_NOT_ALLOWED, message)
    }

    protected fun HttpServletResponse.serverError(message: String) {
        error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message)
    }

    protected fun HttpServletResponse.error(code: Int, message: String) {
        status = code
        contentType = "text/plain"
        write(message)
    }

    protected fun HttpServletResponse.write(content: String) {
        write("text/html", content)
    }

    protected fun HttpServletResponse.write(contentType: String, content: String) {
        this.contentType = contentType
        val bytes = content.toByteArray()
        outputStream.apply {
            write(bytes)
            flush()
        }
    }
}