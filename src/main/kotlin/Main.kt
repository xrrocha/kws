package kws

import org.apache.commons.dbcp2.BasicDataSource
import javax.sql.DataSource

fun main(args: Array<String>) {
    val config = WebappRunnerConfig.fromArgs(args)

    // TODO Add hook to dispose of resources (such as db connections) on server (as opposed to jvm) shutdown
    val applicationContext = SimpleApplicationContext {
        // TODO Add ApplicationContext.register<T>(block: () -> T)
        register<DataSource>(
            BasicDataSource().apply {
                url = "jdbc:postgresql://localhost:5432/willadb?currentSchema=northwind"
                username = "willadb"
                password = "willadb"
                minIdle = 5
                maxIdle = 10
                maxOpenPreparedStatements = 32
            }
        )
    }

    val runner = TomcatWebappRunner(applicationContext)
        .also { it.start(WebappRunnerConfig.fromArgs(args)) }
    Runtime.getRuntime().addShutdownHook(Thread(runner::stop))
    runner.waitOn()
}