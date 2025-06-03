package network.headers

class AcceptLanguageHeader(value: List<String> = emptyList()) : HttpHeader<List<String>>("Accept-Language", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = true
        var isMandatoryOnResponse: Boolean = false
        init { register("Accept-Language", AcceptLanguageHeader::class) }
    }

    override fun validate(): Boolean = value?.isNotEmpty() ?: false

    override fun parse(raw: String) {
        val newValue = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Invalid Accept-Language value: $raw. It must contain at least one language.")
        setValue(newValue)
    }

    override fun serialize(): String = value?.joinToString(", ") ?: ""
}
