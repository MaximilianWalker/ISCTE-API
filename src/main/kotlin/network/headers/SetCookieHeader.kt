package network.headers

class SetCookieHeader(value: String? = null) : HttpHeader<String>("Set-Cookie", value) {
    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) = setValue(raw.trim())

    override fun serialize(): String = value ?: ""
}
