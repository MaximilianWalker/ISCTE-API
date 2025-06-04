package network

import network.headers.ContentLengthHeader
import network.headers.ContentTypeHeader

data class HttpResponse(
    val status: HttpStatus = HttpStatus.OK,
    val headers: HttpHeaders = HttpHeaders(),
    val body: Any? = null,
    var bodyRaw: String? = null
) {
    val contentSerializers: ContentSerializers = ContentSerializers()

    companion object {
        /** Create a 200 OK response */
        fun ok(body: Any? = "", headers: HttpHeaders = HttpHeaders()): HttpResponse {
            return HttpResponse(status = HttpStatus.OK, headers = headers, body = body)
        }

        /** Create a 404 Not Found response */
        fun notFound(body: Any? = "", headers: HttpHeaders = HttpHeaders()): HttpResponse {
            return HttpResponse(status = HttpStatus.NOT_FOUND, headers = headers, body = body)
        }

        /** Create a 500 Internal Server Error response */
        fun internalServerError(body: Any? = "", headers: HttpHeaders = HttpHeaders()): HttpResponse {
            return HttpResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, headers = headers, body = body)
        }
    }

    /** Encode the body based on Content-Type header */
    fun encode() {
        if (body == null) {
            bodyRaw = null
            return
        }
        val contentTypeHeader = headers.get("Content-Type") as? ContentTypeHeader
        val contentTypeValue = contentTypeHeader?.value
            ?: throw IllegalArgumentException("Content-Type header is missing or invalid.")
        bodyRaw = contentSerializers.serialize(contentTypeValue, body)
        val contentLengthHeader = headers.get("Content-Length") as? ContentLengthHeader
        contentLengthHeader?.setValue(bodyRaw!!.length)
    }

    /** Serialize the full HTTP response to a raw string */
    fun serialize(): String {
        val requestRaw = "HTTP/1.1 ${status.code} ${status.reason}"

        headers.fillResponseHeaders()

        if (body != null)
            encode()

        val headersRaw = headers.serialize().map { (name, value) -> "$name: $value" }.joinToString("\r\n")

        val requestList = mutableListOf(requestRaw, headersRaw, "", bodyRaw ?: "")
//        bodyRaw?.let {
//            requestList.add(it)
//        }
        return requestList.joinToString("\r\n")
    }
}
