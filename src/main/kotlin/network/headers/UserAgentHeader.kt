package network.headers

class UserAgentHeader(value: String? = null) : HttpHeader<String>("User-Agent", value) {
    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) = setValue(raw.trim())

    override fun serialize(): String = value ?: ""
}