package network

import converter.JSONConverter
import serializer.JSONSerializer
import serializer.Serializer
import serializers.TextBodySerializer

class ContentSerializers {
    private var jsonSerializer: JSONSerializer? = JSONConverter()
    private var textSerializer: TextBodySerializer? = TextBodySerializer()
    private var htmlSerializer: Serializer<String, String, *>? = null
    private var formSerializer: Serializer<Map<String, String>, String, *>? = null
    private var multipartSerializer: Serializer<ByteArray, String, *>? = null

    fun registerJSON(serializer: JSONSerializer) {
        jsonSerializer = serializer
    }

    fun registerText(serializer: TextBodySerializer) {
        textSerializer = serializer
    }

    fun registerHTML(serializer: Serializer<String, String, *>) {
        htmlSerializer = serializer
    }

    fun registerForm(serializer: Serializer<Map<String, String>, String, *>) {
        formSerializer = serializer
    }

    fun registerMultipart(serializer: Serializer<ByteArray, String, *>) {
        multipartSerializer = serializer
    }

    fun <T> serialize(contentType: ContentType, input: T): String {
        return when (contentType) {
            ContentType.JSON -> {
                val serializer = jsonSerializer ?: throw IllegalStateException("No JSON serializer registered")
                (serializer as Serializer<T, String, *>).serialize(input)
            }
            ContentType.TEXT -> {
                val serializer = textSerializer ?: throw IllegalStateException("No Text serializer registered")
                (serializer as Serializer<T, String, *>).serialize(input)
            }
            ContentType.HTML -> {
                val serializer = htmlSerializer ?: throw IllegalStateException("No HTML serializer registered")
                (serializer as Serializer<T, String, *>).serialize(input)
            }
            ContentType.FORM -> {
                val serializer = formSerializer ?: throw IllegalStateException("No Form serializer registered")
                (serializer as Serializer<T, String, *>).serialize(input)
            }
            ContentType.MULTIPART -> {
                val serializer = multipartSerializer ?: throw IllegalStateException("No Multipart serializer registered")
                (serializer as Serializer<T, String, *>).serialize(input)
            }
            else -> throw NotImplementedError("Serialization for $contentType is not implemented yet")
        }
    }
}