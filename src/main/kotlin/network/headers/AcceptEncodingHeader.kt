package network.headers

class AcceptEncodingHeader(value: List<String> = emptyList()) : HttpHeader<List<String>>("Accept-Encoding", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = true
        var isMandatoryOnResponse: Boolean = false
        init { register("Accept-Encoding", AcceptEncodingHeader::class) }
    }

    override fun validate(): Boolean = value?.isNotEmpty() ?: false

    override fun parse(raw: String) {
        val newValue = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Invalid Accept-Encoding value: $raw. It must contain at least one encoding.")
        setValue(newValue)
    }

    override fun serialize(): String = value?.joinToString(", ") ?: ""
}
