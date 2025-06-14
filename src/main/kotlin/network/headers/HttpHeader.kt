package network.headers

import kotlin.reflect.KClass
import core.instantiateWithDefaultConstructor

import network.headers.AcceptEncodingHeader
import network.headers.AcceptHeader
import network.headers.AcceptLanguageHeader
import network.headers.AuthorizationHeader
import network.headers.CacheControlHeader
import network.headers.ConnectionHeader
import network.headers.ContentLengthHeader
import network.headers.ContentTypeHeader
import network.headers.CookieHeader
import network.headers.HostHeader
import network.headers.ServerHeader
import network.headers.SetCookieHeader
import network.headers.UserAgentHeader

abstract class HttpHeader<T>(val name: String, initialValue: T? = null) {
    var value: T? = initialValue
        private set

    companion object {
        val subclasses: Map<String, KClass<out HttpHeader<*>>> = mapOf(
            "Accept" to AcceptHeader::class,
            "Accept-Encoding" to AcceptEncodingHeader::class,
            "Accept-Language" to AcceptLanguageHeader::class,
            "Authorization" to AuthorizationHeader::class,
            "Cache-Control" to CacheControlHeader::class,
            "Connection" to ConnectionHeader::class,
            "Content-Length" to ContentLengthHeader::class,
            "Content-Type" to ContentTypeHeader::class,
            "Cookie" to CookieHeader::class,
            "Host" to HostHeader::class,
            "Server" to ServerHeader::class,
            "Set-Cookie" to SetCookieHeader::class,
            "User-Agent" to UserAgentHeader::class
        )
        
        // According to HTTP/1.1 spec (RFC 7231), only Host header is mandatory
        val mandatoryRequestHeaders: Set<KClass<out HttpHeader<*>>> = setOf(
            HostHeader::class
        )
        
        val mandatoryResponseHeaders: Set<KClass<out HttpHeader<*>>> = setOf(
            ContentTypeHeader::class,
            ContentLengthHeader::class,
            ConnectionHeader::class,
            CacheControlHeader::class,
            ServerHeader::class
        )

        /**
         * Get header class by header name
         */
        fun getClassByHeader(headerName: String): KClass<out HttpHeader<*>>? = subclasses[headerName]
        
        /**
         * Get the header name for a specific HttpHeader class.
         * @param headerClass The class of the header
         * @return The name of the header, or null if the class is not registered
         */
        fun getHeaderByClass(headerClass: KClass<out HttpHeader<*>>): String? {
            return subclasses.entries.find { it.value == headerClass }?.key
        }
        
        /**
         * Parse a header value based on header name and raw value.
         * @param headerName The name of the header to parse
         * @param rawValue The raw string value of the header
         * @return The parsed header instance, or null if parsing failed or header class not found
         */
        fun parseHeader(headerName: String, rawValue: String): HttpHeader<*>? {
            try {
                val headerClass = getClassByHeader(headerName) ?: return null
                val header = headerClass.instantiateWithDefaultConstructor() ?: return null
                header.parse(rawValue)
                return header
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error parsing unknown header: '$headerName'")
                return null
            }
        }
    }

    fun setValue(newValue: T) {
        val oldValue = value
        value = newValue
        if (!validate()) {
            value = oldValue
            throw IllegalArgumentException("Header value $value is not valid.")
        }
    }

    abstract fun parse(raw: String)
    abstract fun validate(): Boolean
    abstract fun serialize(): String
}
