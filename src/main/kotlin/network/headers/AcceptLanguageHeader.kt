package network.headers

class AcceptLanguageHeader(value: List<String> = emptyList()) : HttpHeader<List<String>>("Accept-Language", value) {
    override fun validate(): Boolean = value?.isNotEmpty() ?: false

    override fun parse(raw: String) {
        val newValue = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Invalid Accept-Language value: $raw. It must contain at least one language.")
        setValue(newValue)
    }

    override fun serialize(): String = value?.joinToString(", ") ?: ""
}
