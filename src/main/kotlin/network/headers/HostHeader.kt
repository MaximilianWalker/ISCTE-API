package network.headers

class HostHeader(value: String? = null) : HttpHeader<String>("Host", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = true
        var isMandatoryOnResponse: Boolean = false
        init { register("Host", HostHeader::class) }
    }

    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) = setValue(raw.trim())

    override fun serialize(): String = value ?: ""
}
