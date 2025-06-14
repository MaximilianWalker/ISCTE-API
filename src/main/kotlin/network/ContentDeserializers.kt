package network

import converter.JSONConverter
import deserializer.Deserializer
import deserializer.JSONDeserializer
import deserializer.TextBodyDeserializer

class ContentDeserializers {
    private var jsonDeserializer: JSONDeserializer? = JSONConverter()
    private var textDeserializer: TextBodyDeserializer? = TextBodyDeserializer()
    private var htmlDeserializer: Deserializer<String, String>? = null
    private var formDeserializer: Deserializer<Map<String, String>, String>? = null
    private var multipartDeserializer: Deserializer<ByteArray, String>? = null

    fun registerJSON(deserializer: JSONDeserializer) {
        jsonDeserializer = deserializer
    }

    fun registerText(deserializer: TextBodyDeserializer) {
        textDeserializer = deserializer
    }

    fun registerHTML(deserializer: Deserializer<String, String>) {
        htmlDeserializer = deserializer
    }

    fun registerForm(deserializer: Deserializer<Map<String, String>, String>) {
        formDeserializer = deserializer
    }

    fun registerMultipart(deserializer: Deserializer<ByteArray, String>) {
        multipartDeserializer = deserializer
    }

    fun <T> deserialize(contentType: ContentType, input: String): T {
        return when (contentType) {
            ContentType.JSON -> {
                val deserializer = jsonDeserializer ?: throw IllegalStateException("No JSON deserializer registered")
                (deserializer as Deserializer<String, *>).deserialize(input) as T
            }
            ContentType.TEXT -> {
                val deserializer = textDeserializer ?: throw IllegalStateException("No Text deserializer registered")
                (deserializer as Deserializer<String, *>).deserialize(input) as T
            }
            ContentType.HTML -> {
                val deserializer = htmlDeserializer ?: throw IllegalStateException("No HTML deserializer registered")
                (deserializer as Deserializer<String, *>).deserialize(input) as T
            }
            ContentType.FORM -> {
                val deserializer = formDeserializer ?: throw IllegalStateException("No Form deserializer registered")
                (deserializer as Deserializer<String, *>).deserialize(input) as T
            }
            ContentType.MULTIPART -> {
                val deserializer = multipartDeserializer ?: throw IllegalStateException("No Multipart deserializer registered")
                (deserializer as Deserializer<String, *>).deserialize(input) as T
            }
            else -> throw NotImplementedError("Deserialization for $contentType is not implemented yet")
        }
    }
}