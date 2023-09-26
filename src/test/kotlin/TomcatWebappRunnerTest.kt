package kws

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class TomcatWebappRunnerTest {
    @Test
    fun `starts tomcat correctly`() {
        val config = WebappRunnerConfig(
            host = "localhost",
            port = 8080,
            baseDirectory = "src/test/resources/webapp",
            contextPath = "",
            servletPath = "servlet",
            contextListenerClass = null,
            requestListenerClass = null
        )
        val twr = TomcatWebappRunner(SimpleApplicationContext(emptyMap<KClass<*>, Any?>()))
        twr.start(config)
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://${config.host}:${config.port}/"))
            .GET()
            .build()
        val expectedIndexPage =
            File("${config.baseDirectory}/index.html")
                .readText()
        val actualIndexPage = client
            .send(request, HttpResponse.BodyHandlers.ofString())
            .body()
        assertEquals(expectedIndexPage, actualIndexPage)
        twr.stop()
    }
}