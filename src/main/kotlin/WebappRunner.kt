package kws

import jakarta.servlet.ServletConfig
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.catalina.startup.Tomcat
import java.io.File

interface WebappRunner {
    fun start(config: WebappRunnerConfig)
    fun waitOn()
    fun stop()
}

class TomcatWebappRunner(
    private val applicationContext: ApplicationContext
) : WebappRunner {

    private val tomcat by lazy { Tomcat() }

    override fun start(config: WebappRunnerConfig) {
        tomcat.apply {
            setBaseDir(config.baseDirectory)
            setPort(config.port)
            setHostname(config.host)
            getConnector()
        }

        val ctx = tomcat.addWebapp(
            "/${config.contextPath}",
            "${File(config.baseDirectory).absolutePath}/${config.contextPath}"
        )

        config.contextListenerClass?.also(ctx::addApplicationListener)
        config.requestListenerClass?.also(ctx::addApplicationListener)


        ctx.addServletContainerInitializer({ _, srvCtx ->
            srvCtx.setAttribute(
                ApplicationContext.KEY_NAME, applicationContext
            )
        }, emptySet())

        val servletManagerName = WebappRunnerServlet::class.simpleName
        Tomcat.addServlet(ctx, servletManagerName, WebappRunnerServlet())
        ctx.addServletMappingDecoded(
            "/${config.servletPath}/*", servletManagerName
        )

        // Run JVM with:
        //  --add-opens=java.base/java.lang=ALL-UNNAMED
        //  --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED
        tomcat.start()
    }

    override fun waitOn() {
        tomcat.server.await()
    }

    override fun stop() {
        tomcat.stop()
    }
}

class WebappRunnerServlet : HttpServlet() {
    private val servlets = mutableMapOf<String, HttpServlet>()

    override fun init(config: ServletConfig) {
        super.init(config)
    }

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        val servletName = req.pathInfo.substring(1)
        val servlet = servlets.computeIfAbsent(servletName) {
            (Class.forName(servletName).getConstructor().newInstance() as HttpServlet)
                .also { it.init(servletConfig) }
        }
        servlet.service(req, resp)
    }
}

data class WebappRunnerConfig(
    val host: String,
    val port: Int,
    val baseDirectory: String,
    val contextPath: String,
    val servletPath: String,
    val contextListenerClass: String?,
    val requestListenerClass: String?
) {
    companion object {
        fun fromArgs(args: Array<String>): WebappRunnerConfig {
            val argMap = mapFromArgs(args)
            val host = argMap.getOrDefault("host", "localhost")
            val port: Int =
                argMap.getOrDefault("port", "8080").toInt()
            val baseDirectory =
                argMap.getOrDefault(
                    "directory",
                    System.getProperty("user.dir")
                )
            val contextPath = argMap.getOrDefault("context-path", "")
            val servletPath = argMap.getOrDefault("servlet-path", "servlet")
            val contextListener = argMap["context-listener"]
            val requestListener = argMap["request-listener"]
            return WebappRunnerConfig(
                host,
                port,
                baseDirectory,
                contextPath,
                servletPath,
                contextListener,
                requestListener
            )
        }

        private fun mapFromArgs(args: Array<String>): Map<String, String> =
            args
                .filter { it.startsWith("--") && it.contains("=") }
                .associate {
                    val pos = it.indexOf('=')
                    val name = it.substring(0, pos)
                    val value = it.substring(pos + 1)
                    name to value
                }
    }
}