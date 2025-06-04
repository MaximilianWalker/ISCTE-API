package network.headers

import network.ContentType

class AcceptHeader(value: List<ContentType> = emptyList()) : HttpHeader<List<ContentType>>("Accept", value) {
    // Custom class to represent content types including wildcards
    data class AcceptContentType(val type: String, val subtype: String) {
        val value: String
            get() = "$type/$subtype"
        
        fun matches(contentType: ContentType): Boolean {
            if (type == "*" && subtype == "*") return true
            if (type == "*") return false // Invalid pattern
            if (subtype == "*") return contentType.value.startsWith("$type/")
            return contentType.value.equals(value, ignoreCase = true)
        }
        
        companion object {
            fun parse(value: String): AcceptContentType? {
                val parts = value.trim().split("/", limit = 2)
                if (parts.size != 2) return null
                return AcceptContentType(parts[0].lowercase(), parts[1].lowercase())
            }
        }
    }
    
    override fun validate(): Boolean = value?.isNotEmpty() ?: false

    override fun parse(raw: String) {
        // Parse Accept header value which might include wildcards like */* or application/*
        val acceptTypes = raw.split(",").mapNotNull { part ->
            val acceptTypePart = part.trim()
            
            // For exact matches, try to use the enum
            val enumType = ContentType.fromHeader(acceptTypePart)
            if (enumType != null) {
                return@mapNotNull enumType
            }
            
            // For wildcards or types not in enum, try to find the closest match
            val parsedType = AcceptContentType.parse(acceptTypePart)
            if (parsedType != null) {
                // Find best match or default to TEXT
                ContentType.entries.find { parsedType.matches(it) } ?: ContentType.TEXT
            } else null
        }
        
        if (acceptTypes.isEmpty()) {
            throw IllegalArgumentException("Invalid Accept value: $raw. It must contain at least one valid Content-Type.")
        }
        setValue(acceptTypes)
    }

    override fun serialize(): String = value?.joinToString(", ") { it.value } ?: ""
}
