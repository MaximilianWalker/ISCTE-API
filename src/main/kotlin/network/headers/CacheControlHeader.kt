package network.headers

class CacheControlHeader(value: List<String> = emptyList()) : HttpHeader<List<String>>("Cache-Control", value) {
    override fun validate(): Boolean = value?.isNotEmpty() ?: false

    override fun parse(raw: String) {
        val newValue = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Invalid Cache-Control value: $raw. It must contain at least one directive.")
        setValue(newValue)
    }

    override fun serialize(): String = value?.joinToString(", ") ?: ""
}
