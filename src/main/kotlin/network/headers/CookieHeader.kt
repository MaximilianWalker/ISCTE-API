package network.headers

class CookieHeader(value: Map<String, String> = emptyMap()) : HttpHeader<Map<String, String>>("Cookie", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = false
        var isMandatoryOnResponse: Boolean = false
        init { register("Cookie", CookieHeader::class) }
    }

    override fun validate(): Boolean = value?.isNotEmpty() ?: false

    override fun parse(raw: String) {
        val pairs = raw.split(";").mapNotNull {
            val (k, v) = it.trim().split("=", limit = 2).map(String::trim)
            if (k.isNotEmpty() && v.isNotEmpty()) k to v else null
        }
        if (pairs.isEmpty())
            throw IllegalArgumentException("Invalid Cookie header value: $raw")
        setValue(pairs.toMap())
    }

    override fun serialize(): String =
        value?.entries?.joinToString("; ") { "${it.key}=${it.value}" } ?: ""
}
