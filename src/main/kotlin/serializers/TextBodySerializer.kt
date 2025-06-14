package serializers

import serializer.TextSerializer

class TextBodySerializer : TextSerializer<String, Unit> {
    override fun serialize(input: String): String = input

    override fun serializeWithOptions(input: String, options: Unit): String = input
}