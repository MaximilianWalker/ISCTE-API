package network

enum class HttpStatus(val code: Int, val reason: String) {
    // 1xx Informational
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    // 2xx Success
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),

    // 3xx Redirection
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),

    // 4xx Client Errors
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    IM_A_TEAPOT(418, "I'm a teapot"),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    override fun toString(): String = "$code $reason"

    companion object {
        fun fromCode(code: Int): HttpStatus? = entries.firstOrNull { it.code == code }
    }
}