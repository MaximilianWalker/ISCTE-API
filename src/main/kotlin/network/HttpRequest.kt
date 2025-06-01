package network

import java.io.BufferedReader

data class HttpRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val body: String?
) {
    companion object {
        fun parse(input: BufferedReader): HttpRequest {
            val requestLine = input.readLine() ?: throw IllegalArgumentException("Invalid request")
            val (method, path, _) = requestLine.split(" ")

            val headers = mutableMapOf<String, String>()
            var line: String?
            while (input.readLine().also { line = it } != "") {
                val (key, value) = line!!.split(": ", limit = 2)
                headers[key] = value
            }

            val contentLength = headers["Content-Length"]?.toIntOrNull() ?: 0
            val body = if (contentLength > 0) {
                val charArray = CharArray(contentLength)
                input.read(charArray, 0, contentLength)
                String(charArray)
            } else null

            return HttpRequest(method, path, headers, body)
        }
    }
}