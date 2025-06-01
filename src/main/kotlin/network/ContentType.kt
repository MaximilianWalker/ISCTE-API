package network;

enum class ContentType(val value: String) {
    JSON("application/json"),
    TEXT("text/plain"),
    HTML("text/html"),
    FORM("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data");

    companion object {
        fun fromHeader(value: String?): ContentType? {
            return entries.firstOrNull { value?.startsWith(it.value, ignoreCase = true) == true }
        }
    }
}