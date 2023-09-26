package kws

import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import java.sql.DriverManager

fun main(args: Array<String>) {
    val config = WebappRunnerConfig.fromArgs(args)

    val applicationContext = SimpleApplicationContext {
        register(
            DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/willadb?currentSchema=northwind",
                "willadb", "willadb"
            )
        )
    }

    val runner = TomcatWebappRunner(applicationContext)
        .also { it.start(WebappRunnerConfig.fromArgs(args)) }
    Runtime.getRuntime().addShutdownHook(Thread(runner::stop))
    runner.waitOn()
}