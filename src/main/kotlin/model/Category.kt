package kws.model

import kws.Jdbc
import javax.sql.DataSource

/*
@Suppress("unchecked_cast")
operator fun <P> Any?.getValue(
    thisRef: Any?,
    property: KProperty<*>
): P = this as P
 */

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
                Jdbc.executeQuery(connection) { selectSql }
                    .map { row ->
                        val map = row
                            .map { (name, value) ->
                                (nameMappings[name] ?: Jdbc.snake2CamelCase(name)) to value
                            }
                            .toMap()
                            .toMutableMap()
                        Category(
                            map
                                .withDefault { propertyName ->
                                    map.computeIfAbsent(propertyName) {
                                        val (names, block) = childMappings[propertyName]!!
                                        val params = names.associateWith { map[it] }
                                        block(params, dataSource)
                                    }
                                }
                        )
                    }
            }

        val tableName = "categories"
        val pkColumns = listOf("id")
        val naturalUniqueColumns = listOf("name")
        val descriptorColumns = listOf("description")
        val orderColumns = listOf("category_name")
        val fkColumns = emptyList<Any>() // TODO Define FK's
    }
}