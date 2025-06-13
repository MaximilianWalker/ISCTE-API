package deserializer

class TextBodyDeserializer : TextDeserializer<String> {
    override fun deserialize(input: String): String = input
}