package network.headers

class UserAgentHeader(value: String? = null) : HttpHeader<String>("User-Agent", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = true
        var isMandatoryOnResponse: Boolean = false
        init { register("User-Agent", UserAgentHeader::class) }
    }

    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) = setValue(raw.trim())

    override fun serialize(): String = value ?: ""
}