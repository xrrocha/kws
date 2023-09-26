package kws

import kotlin.reflect.KClass

interface ApplicationContext {
    fun <T : Any> lookup(kClass: KClass<T>): T
    operator fun <T : Any> get(kClass: KClass<T>): T = lookup(kClass)

    companion object {
        const val KEY_NAME = "applicationContext"
    }
}

inline fun <reified T : Any> ApplicationContext.lookup(): T = lookup(T::class)

class SimpleApplicationContext(
    private val mappings: Map<KClass<*>, *>
) : ApplicationContext {
    @Suppress("unchecked_cast")
    override fun <T : Any> lookup(kClass: KClass<T>): T =
        (mappings[kClass] as T?)
            ?: throw NoSuchElementException("No registered instance for ${kClass.qualifiedName}")

    companion object {
        open class Builder {
            val mappings = mutableMapOf<KClass<*>, Any>()
            inline fun <reified C : Any> register(instance: C) {
                mappings[C::class] = instance
            }
        }

        operator fun invoke(block: Builder.() -> Unit): ApplicationContext =
            SimpleApplicationContext(Builder().also(block).mappings)
    }
}