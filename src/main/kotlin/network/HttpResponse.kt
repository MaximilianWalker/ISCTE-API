package network

import converter.JSONConverter
import serializer.JSONSerializer

data class HttpResponse(
    val status: HttpStatus = HttpStatus.OK,
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = ""
) {
    val contentSerializers: ContentSerializers = ContentSerializers()

    fun encode(contentType: ContentType, body: Any?): String {
        return when (contentType) {
            ContentType.JSON -> encodeJson(body)
            else -> throw UnsupportedOperationException("Encoding for $contentType not implemented yet.")
        }
    }

    private fun encodeJson(body: Any?): String {
        val serializer = jsonSerializer
            ?: throw IllegalStateException("No JSON serializer configured in BodyEncoder.")

        return serializer(body)
    }

    fun toRawResponse(): String {
        val contentType = headers["Content-Type"]?.let { ContentType.values().find { it.value == it } }
        val formattedBody = contentType?.encode(body) ?: body.toString()

        val statusLine = "HTTP/1.1 ${status.code} ${status.reason}"
        val headerLines = headers.entries.joinToString("\r\n") { "${it.key}: ${it.value}" }
        return listOf(statusLine, headerLines, "", body).joinToString("\r\n")
    }
}