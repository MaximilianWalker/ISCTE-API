package network

data class HttpServerOptions(
    val maxConnections: Int = 100,               // Max queued socket connections
    val connectionTimeoutMillis: Long = 30_000,  // Timeout per client (30s)
    val logRequests: Boolean = true,             // Log requests to console
    val enableKeepAlive: Boolean = false,        // Simple HTTP keep-alive toggle
    val serverName: String = "KotlinHttpServer", // Server header value
    val maxPortAttempts: Int = 10                // Maximum attempts to find an available port
)
