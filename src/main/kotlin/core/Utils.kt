package core

import kotlin.reflect.*
import kotlin.reflect.full.primaryConstructor

val <T : Any> KClass<T>.primaryConstructor: KFunction<T>
    get() {
        return constructors.maxByOrNull { it.parameters.size } as? KFunction<T>
            ?: throw NoSuchElementException("No suitable primary constructor found for ${this.simpleName}")
    }

val <T : Any> KClass<T>.properties: List<KProperty<T>>
    get() {
        return members.filterIsInstance<KProperty<T>>()
    }

fun <T : Any> KFunction<T>.getParameter(name: String): KParameter {
    return parameters.find { it.name == name }
        ?: throw NoSuchElementException("Parameter '$name' not found in function '${this.name}'.")
}

fun <T : Any> KClass<T>.getProperty(name: String): KProperty<T> {
    return this.properties.find { it.name == name }
        ?: throw NoSuchElementException("Property '$name' not found in class '${this.simpleName}'.")
}

fun <T : Any> KClass<T>.getProperty1(name: String): KProperty1<T, *> {
    return getProperty(name) as KProperty1<T, *>
}

fun KType.getFirstTypeArgument(): KType {
    return this.arguments.firstOrNull()?.type
        ?: throw IllegalArgumentException("Could not determine first type argument for $this")
}

fun KType.getSecondTypeArgument(): KType {
    return this.arguments[1].type
        ?: throw IllegalArgumentException("Could not determine second type argument for $this")
}

val <T : Any> KClass<T>.defaultConstructor: KFunction<T>?
    get() {
        val constructors = this.constructors
        return constructors.find { constructor ->
            val parameters = constructor.parameters
            parameters.isEmpty() || parameters.all { it.isOptional || it.type.isMarkedNullable }
        }
    }

fun <T : Any> KClass<T>.instantiateWithDefaultConstructor(): T? {
    return defaultConstructor?.callBy(emptyMap())
}