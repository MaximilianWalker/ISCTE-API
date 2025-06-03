package network.headers

class UnknownHeader(headerName: String, value: String? = null) : HttpHeader<String>(headerName, value) {

    companion object {
        var isMandatoryOnRequest: Boolean = false
        var isMandatoryOnResponse: Boolean = false
    }

    override fun validate(): Boolean = true

    override fun parse(raw: String) {
        setValue(raw.trim())
    }

    override fun serialize(): String = value ?: ""
}