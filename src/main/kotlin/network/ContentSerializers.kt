package network

import converter.JSONConverter
import serializer.JSONSerializer
import serializer.Serializer

class ContentSerializers {
    private val serializers: MutableMap<ContentType, Serializer<*, *, *>> = mutableMapOf(
        ContentType.JSON to JSONConverter()
    )

    fun register(contentType: ContentType, serializer: Serializer<*, *, *>) {
        when (contentType) {
            ContentType.JSON -> require(serializer is JSONSerializer) {
                "JSON must implement JsonContentSerializer"
            }

            else -> throw IllegalArgumentException("Unsupported content type: $contentType")
        }
        serializers[contentType] = serializer
    }

    fun get(contentType: ContentType): Serializer<*, *, *> {
        return serializers[contentType]
            ?: throw IllegalStateException("No serializer registered for content type: $contentType")
    }

    fun clear() {
        serializers.clear()
    }
}