package network.headers

import network.ContentType

class AcceptHeader(value: List<ContentType> = emptyList()) : HttpHeader<List<ContentType>>("Accept", value) {
    override fun validate(): Boolean = value?.all { ContentType.fromHeader(it.value) != null } ?: false

    override fun parse(raw: String) {
        val list = raw.split(",").mapNotNull { ContentType.fromHeader(it.trim()) }
        if (list.isEmpty()) {
            throw IllegalArgumentException("Invalid Accept value: $raw. It must contain at least one valid Content-Type.")
        }
        setValue(list)
    }

    override fun serialize(): String = value?.joinToString(", ") { it.value } ?: ""
}
