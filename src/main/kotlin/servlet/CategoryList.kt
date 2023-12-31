package kws.servlet

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.html.*
import kws.BaseServlet
import kws.model.Category

class CategoryList : BaseServlet() {
    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        val categories = withConnection(Category::findAll)
        println(categories.first().products)
        resp.write(render(categories))
    }

    private fun render(categories: List<Category>): String =
        html {
            head { title("Categories") }
            body {
                fieldSet {
                    style = """
                        display: inline-block;
                        margin-left: auto; margin-right: auto;
                    """.trimIndent()
                    legend {
                        h2 { +"Categories" }
                    }
                    ul {
                        categories.map { category ->
                            li {
                                strong { +category.name }
                                +": ${category.description}"
                            }
                        }
                    }
                }
            }
        }
}