package kws

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

object Jdbc {
    private val ParamNameRE = ":[_\\p{IsLatin}][_\\p{IsLatin}\\d]+".toRegex()

    fun executeQuery(
        connection: Connection,
        labelMapper: (String) -> String = { it },
        selectSql: () -> String
    ) = executeQuery(connection, labelMapper, emptyMap(), selectSql)

    fun executeQuery(
        connection: Connection,
        labelMapper: (String) -> String = { it },
        selectParams: Map<String, Any?> = emptyMap(),
        selectSql: () -> String
    ): List<MutableMap<String, Any?>> =
        connection
            .prepareStatement(selectSql(), selectParams)
            .executeQuery()
            .collect(labelMapper)

    private fun Connection.prepareStatement(
        select: String,
        params: Map<String, Any?>
    ): PreparedStatement {
        val paramReferences =
            ParamNameRE
                .findAll(select)
                .map { mr -> mr.value.substring(1) }
                .toList()
        val paramSelect = ParamNameRE.replace(select, "?")
        val statement = prepareStatement(paramSelect)
        val paramMetaData = statement.parameterMetaData
        paramReferences.withIndex().forEach() { (idx, paramName) ->
            val paramValue = params[paramName]
            if (paramValue != null) {
                statement.setObject(idx + 1, paramValue)
            } else {
                statement.setNull(
                    idx + 1,
                    paramMetaData.getParameterType(idx + 1)
                )
            }
        }
        return statement
    }

    private fun ResultSet.collect(
        labelMapper: (String) -> String = { it }
    ): List<MutableMap<String, Any?>> {
        val labels = (1..metaData.columnCount)
            .map(metaData::getColumnLabel)
            .map { label -> labelMapper(label) to label }
        return generateSequence { this }
            .takeWhile { it.next() }
            .map { rs ->
                labels.associate { (name, label) ->
                    name to rs.getObject(label)
                }
                    .toMutableMap()
            }
            .toList()
    }

    fun snake2CamelCase(snakeCase: String): String =
        snakeCase
            .lowercase()
            .split("_")
            .filterNot { it.isEmpty() }
            .joinToString("") {
                it.substring(0, 1).uppercase() + it.substring(1)
            }
            .let {
                it.substring(0, 1).lowercase() + it.substring(1)
            }
}