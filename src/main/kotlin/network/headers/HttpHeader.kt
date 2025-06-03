package network.headers

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import core.instantiateWithDefaultConstructor

abstract class HttpHeader<T>(val name: String, initialValue: T? = null) {
    var value: T? = initialValue
        private set

    companion object {
        var isMandatoryOnRequest: Boolean = false
            private set
        var isMandatoryOnResponse: Boolean = false
            private set

        private val subclasses = mutableMapOf<String, KClass<out HttpHeader<*>>>()

        fun register(name: String, clazz: KClass<out HttpHeader<*>>) {
            subclasses[name] = clazz
        }

        fun getSubclasses(): List<KClass<out HttpHeader<*>>> = subclasses.values.toList()

        fun getClassByHeader(headerName: String): KClass<out HttpHeader<*>>? = subclasses[headerName]

        fun setMandatoryOnRequest(mandatory: Boolean) {
            isMandatoryOnRequest = mandatory
        }

        fun setMandatoryOnResponse(mandatory: Boolean) {
            isMandatoryOnResponse = mandatory
        }
        
        /**
         * Parse a header value based on header name and raw value.
         * @param headerName The name of the header to parse
         * @param rawValue The raw string value of the header
         * @return The parsed header instance, or null if parsing failed or header class not found
         */
        fun parseHeader(headerName: String, rawValue: String): HttpHeader<*>? {
            try {
                val headerClass = getClassByHeader(headerName) ?: return null
                val header = headerClass.instantiateWithDefaultConstructor() ?: return null
                header.parse(rawValue)
                return header
            } catch (e: Exception) {
                println("Error parsing unknown header: '$headerName'")
                return null
            }
        }

        fun getMandatoryRequestHeaders(): List<KClass<out HttpHeader<*>>> {
            return getSubclasses().filter { clazz ->
                val companion = clazz.companionObject
                    ?: throw IllegalStateException("Header class ${clazz.simpleName} is missing companion object")
                val property = companion.members.find { it.name == "isMandatoryOnRequest" } as? KProperty<*>
                    ?: throw IllegalStateException("Header class ${clazz.simpleName} is missing isMandatoryOnRequest property")
                property.getter.call(companion.objectInstance) as? Boolean
                    ?: throw IllegalStateException("Header class ${clazz.simpleName} has invalid isMandatoryOnRequest value")
            }
        }

        fun getMandatoryResponseHeaders(): List<KClass<out HttpHeader<*>>> {
            return getSubclasses().filter { clazz ->
                val companion = clazz.companionObject
                    ?: throw IllegalStateException("Header class ${clazz.simpleName} is missing companion object")
                val property = companion.members.find { it.name == "isMandatoryOnResponse" } as? KProperty<*>
                    ?: throw IllegalStateException("Header class ${clazz.simpleName} is missing isMandatoryOnResponse property")
                property.getter.call(companion.objectInstance) as? Boolean
                    ?: throw IllegalStateException("Header class ${clazz.simpleName} has invalid isMandatoryOnResponse value")
            }
        }
    }

    fun setValue(newValue: T) {
        val oldValue = value
        value = newValue
        if (!validate()) {
            value = oldValue
            throw IllegalArgumentException("Header value $value is not valid.")
        }
    }

    abstract fun parse(raw: String)
    abstract fun validate(): Boolean
    abstract fun serialize(): String
}
