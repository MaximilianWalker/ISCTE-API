package network;

enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE, CONNECT;

    companion object {
        fun fromString(value: String): HttpMethod =
            try {
                value.uppercase().let { method -> entries.first { it.name == method } }
            } catch (e: Exception) {
                throw IllegalArgumentException("Unsupported HTTP method: $value")
            }
    }
}