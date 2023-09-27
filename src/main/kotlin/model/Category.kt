package kws.model

import kws.Jdbc
import kws.Jdbc.snake2CamelCase
import javax.sql.DataSource

class Category(map: Map<String, Any?>) {
    val id: Int by map
    val name: String by map
    val description: String by map
    val picture: ByteArray by map
    val products: List<Any> by map

    companion object {

        private val selectSql = """
            SELECT  *
            FROM categories
            ORDER BY category_name
        """.trimIndent()

        private val nameMappings = mapOf(
            "category_id" to "id",
            "category_name" to "name",
        )

        private val childMappings = mapOf<String, Pair<Set<String>, (Map<String, Any?>, DataSource) -> Any>>(
            "products" to Pair(setOf("id")) { params, dataSource ->
                dataSource.connection.use { connection ->
                    Jdbc.executeQuery(connection) {
                        """
                            SELECT *
                            FROM products
                            WHERE category_id = ${params["id"]}
                        """.trimIndent()
                    }
                }
            }
        )

        fun findAll(dataSource: DataSource): List<Category> =
            dataSource.connection.use { connection ->
                // TODO Jdbc.executeQuery(Connection, String)
                Jdbc.executeQuery(connection) { selectSql }
                    .map { row ->
                        val map = row
                            .map { (name, value) ->
                                val newName =
                                    nameMappings[name] ?: snake2CamelCase(name)
                                newName to value
                            }
                            .toMap()
                            .toMutableMap()
                        map.withDefault { propertyName ->
                            // TODO Can be one (Map<String, Any?>) or many (List<Map<String, Any?>>
                            map.computeIfAbsent(propertyName) {
                                val (keyNames, block) =
                                    childMappings[propertyName]!!
                                val params =
                                    keyNames.associateWith { map[it] }
                                block(params, dataSource)
                            }
                        }
                    }
                    .map(::Category)
            }

        val tableName = "categories"
        val pkColumns = listOf("id")
        val naturalUniqueColumns = listOf("name")
        val descriptorColumns = listOf("description")
        val orderColumns = listOf("category_name")
        val fkColumns = emptyList<Any>() // TODO Define FK's
    }
}