package kws.model

import kws.Jdbc
import java.sql.Connection
import kotlin.reflect.KProperty

@Suppress("unchecked_cast")
operator fun <P> Any?.getValue(
    thisRef: Any?,
    property: KProperty<*>
): P = this as P

class Category(private val map: Map<String, Any?>) {
    val id: Int by map["category_id"]
    val name: String by map["category_name"]
    val description: String by map["description"]
    val picture: ByteArray by map["bytea"]

    companion object {
        private val selectSql = """
            SELECT  *
            FROM categories
            ORDER BY category_name
        """.trimIndent()

        fun findAll(connection: Connection): List<Category> =
            Jdbc.executeQuery(connection) { selectSql }
                .map(::Category)
    }
}