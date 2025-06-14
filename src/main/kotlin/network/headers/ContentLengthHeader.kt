package network.headers

class ContentLengthHeader(value: Int = 0) : HttpHeader<Int>("Content-Length", value) {
    override fun validate(): Boolean = value!! >= 0

    override fun parse(raw: String) {
        val newValue = raw.toIntOrNull()?.takeIf { it >= 0 } 
            ?: throw IllegalArgumentException("Invalid Content-Length value: $raw")
        setValue(newValue)
    }

    override fun serialize(): String = value.toString()
}
