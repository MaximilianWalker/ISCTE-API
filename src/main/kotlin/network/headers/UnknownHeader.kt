package network.headers

class UnknownHeader(headerName: String, value: String? = null) : HttpHeader<String>(headerName, value) {
    override fun validate(): Boolean = true

    override fun parse(raw: String) {
        setValue(raw.trim())
    }

    override fun serialize(): String = value ?: ""
}