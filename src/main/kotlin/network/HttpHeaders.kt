package network

import core.instantiateWithDefaultConstructor
import network.headers.UnknownHeader
import network.headers.HttpHeader

class HttpHeaders(headers: List<HttpHeader<*>> = emptyList()) {
    private val headers: MutableMap<String, HttpHeader<*>> = mutableMapOf()

    constructor(vararg elements: HttpHeader<*>) : this(elements.toList())

    init {
        headers.forEach { add(it) }
    }

    companion object {
        /**
         * Parse headers from raw string map and create a new HttpHeaders object.
         * @param rawHeaders Map of header name to header value
         * @return A new HttpHeaders object with parsed headers, or null if parsing failed
         */
        fun parse(rawHeaders: Map<String, String>): HttpHeaders {
            val headers = HttpHeaders()
            
            // Process all raw headers
            for ((rawName, rawValue) in rawHeaders) {
                // Use the existing parseHeader method from HttpHeader
                val header = HttpHeader.parseHeader(rawName, rawValue)
                if (header != null) {
                    headers.add(header)
                } else {
                    // Unknown header - create a GenericHeader for it
                    val genericHeader = UnknownHeader(rawName, rawValue)
                    headers.add(genericHeader)
                }
            }
            
            // Validate mandatory headers are present
            val mandatoryHeaders = HttpHeader.getMandatoryRequestHeaders()
            for (headerClass in mandatoryHeaders) {
                val headerName = headerClass.simpleName?.replace("Header", "") ?: ""
                
                if (!headers.headers.keys.any { it.equals(headerName, ignoreCase = true) }) {
                    throw IllegalArgumentException("Invalid request: Mandatory header $headerName is missing.")
                }
            }
            
            return headers
        }
    }

    /** Add or replace a header */
    fun add(header: HttpHeader<*>) {
        headers[header.name.lowercase()] = header
    }

    /** Get header by name (case-insensitive) */
    fun get(name: String): HttpHeader<*>? = headers[name.lowercase()]

    /** Serialize all headers to a map of name -> serialized value (string) */
    fun serialize(): Map<String, String> {
        val mandatoryHeaders = HttpHeader.getMandatoryResponseHeaders()
        
        // Check for missing mandatory headers and add them if possible
        mandatoryHeaders.forEach { headerClass ->
            val headerName = headerClass.simpleName?.replace("Header", "") ?: ""
            val key = headerName.lowercase()
            if (!headers.containsKey(key)) {
                try {
                    // Try to create a default instance of the header
                    val defaultHeader = headerClass.instantiateWithDefaultConstructor()
                    
                    if (defaultHeader != null) {
                        add(defaultHeader)
                    } else {
                        throw IllegalArgumentException("Mandatory header $headerName is missing and has no default constructor.")
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Mandatory header $headerName is missing and could not be created: ${e.message}")
                }
            }
        }
        
        // Convert headers to map of strings
        return headers.mapNotNull { (key, header) ->
            val serializedValue = header.serialize()
            if (serializedValue.isNotEmpty()) header.name to serializedValue else null
        }.toMap()
    }
}
