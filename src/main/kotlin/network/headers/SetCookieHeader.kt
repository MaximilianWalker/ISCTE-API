package network.headers

class SetCookieHeader(value: String? = null) : HttpHeader<String>("Set-Cookie", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = false
        var isMandatoryOnResponse: Boolean = false
        init { register("Set-Cookie", SetCookieHeader::class) }
    }

    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) = setValue(raw.trim())

    override fun serialize(): String = value ?: ""
}
