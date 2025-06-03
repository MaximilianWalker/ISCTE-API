package network

// Represents standard header definitions (like an enum)
class StandardHttpHeader private constructor(
    val name: String,
    val required: Boolean = false,
    val defaultValue: String? = null
) {
    override fun toString(): String = name

    companion object {
        private val headers = mutableMapOf<String, StandardHttpHeader>()

        // Common HTTP headers
        val HOST = register("Host", required = true)
        val USER_AGENT = register("User-Agent")
        val ACCEPT = register("Accept", defaultValue = "*/*")
        val CONTENT_TYPE = register("Content-Type")
        val CONTENT_LENGTH = register("Content-Length", defaultValue = "0")
        val AUTHORIZATION = register("Authorization")
        val CACHE_CONTROL = register("Cache-Control")
        val CONNECTION = register("Connection", defaultValue = "keep-alive")
        val ACCEPT_ENCODING = register("Accept-Encoding", defaultValue = "gzip, deflate")
        val ACCEPT_LANGUAGE = register("Accept-Language")
        val COOKIE = register("Cookie")
        val REFERER = register("Referer")
        val UPGRADE_INSECURE_REQUESTS = register("Upgrade-Insecure-Requests")
        val IF_MODIFIED_SINCE = register("If-Modified-Since")
        val RANGE = register("Range")
        val TRANSFER_ENCODING = register("Transfer-Encoding")

        private fun register(
            name: String,
            required: Boolean = false,
            defaultValue: String? = null
        ): StandardHttpHeader {
            val header = StandardHttpHeader(name, required, defaultValue)
            headers[name.lowercase()] = header
            return header
        }

        fun fromName(name: String): StandardHttpHeader? =
            headers[name.lowercase()]

        fun all(): List<StandardHttpHeader> = headers.values.toList()

        fun requiredHeaders(): List<StandardHttpHeader> =
            headers.values.filter { it.required }
    }
}
