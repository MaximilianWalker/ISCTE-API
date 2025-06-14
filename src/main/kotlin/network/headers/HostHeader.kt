package network.headers

class HostHeader(value: String? = null) : HttpHeader<String>("Host", value) {
    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) = setValue(raw.trim())

    override fun serialize(): String = value ?: ""
}
