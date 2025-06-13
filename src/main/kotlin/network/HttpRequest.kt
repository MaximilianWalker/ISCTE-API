package network

import network.headers.ContentLengthHeader
import network.headers.ContentTypeHeader

data class HttpRequest(
    val method: HttpMethod,
    val path: String,
    val headers: HttpHeaders,
    val body: Any?,
    var bodyRaw: String? = null,
    val queryParams: Map<String, String> = emptyMap(),
    val contentDeserializers: ContentDeserializers = ContentDeserializers()
) {
    companion object {
        fun parse(
            rawRequest: String,
            contentDeserializers: ContentDeserializers = ContentDeserializers()
        ): HttpRequest {
            println(rawRequest)
            val (requestLine, headerLines, bodyLines) = splitRawRequest(rawRequest)

            val (methodString, path) = parseRequestLine(requestLine)

            val method = HttpMethod.fromString(methodString)

            val headers = parseHeaders(headerLines)

            val contentType = (headers.get("Content-Type") as ContentTypeHeader?)?.value
            val contentLength = (headers.get("Content-Length") as ContentLengthHeader?)?.value ?: 0

            var bodyRaw: String? = null
            var body: Any? = null

            if (contentType != null && contentLength > 0 && bodyLines.isNotEmpty()){
                 bodyRaw = bodyLines.joinToString("\n")
                 body = contentDeserializers.deserialize(contentType, bodyRaw)
            }

            val (cleanPath, queryParams) = extractQueryParams(path)

            return HttpRequest(
                method,
                cleanPath,
                headers,
                body,
                bodyRaw,
                queryParams = queryParams,
                contentDeserializers = contentDeserializers
            )
        }

        private fun extractQueryParams(path: String): Pair<String, Map<String, String>> {
            val parts = path.split("?", limit = 2)
            if (parts.size < 2) return path to emptyMap()

            val cleanPath = parts[0]
            val queryString = parts[1]
            val queryParams = queryString.split("&").mapNotNull {
                val param = it.split("=", limit = 2)
                if (param.size == 2) param[0] to param[1] else null
            }.toMap()

            return cleanPath to queryParams
        }

        private fun splitRawRequest(raw: String): Triple<String, List<String>, List<String>> {
            val lines = raw.split("\r\n", "\n")
            if (lines.isEmpty()) throw IllegalArgumentException("Invalid request")
            val requestLine = lines[0]
            val emptyLineIndex = lines.indexOfFirst { it.isEmpty() }
            val headerLines =
                if (emptyLineIndex != -1) lines.subList(1, emptyLineIndex) else lines.subList(1, lines.size)
            val bodyLines = if (emptyLineIndex != -1) lines.subList(emptyLineIndex + 1, lines.size) else emptyList()

            return Triple(requestLine, headerLines, bodyLines)
        }

        private fun parseRequestLine(line: String): Pair<String, String> {
            val parts = line.split(" ")
            if (parts.size < 2) throw IllegalArgumentException("Invalid request line")
            return parts[0] to parts[1]
        }

        private fun parseHeaders(headerLines: List<String>): HttpHeaders {
            val headers = mutableMapOf<String, String>()
            for (line in headerLines) {
                val splitIndex = line.indexOf(": ")
                if (splitIndex > 0) {
                    val key = line.substring(0, splitIndex)
                    val value = line.substring(splitIndex + 2)
                    headers[key] = value
                }
            }
            return HttpHeaders.parse(headers)
        }
    }
}
