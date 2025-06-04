package network.headers

class AuthorizationHeader(value: String? = null) : HttpHeader<String>("Authorization", value) {
    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) = setValue(raw.trim())

    override fun serialize(): String = value ?: ""
}
