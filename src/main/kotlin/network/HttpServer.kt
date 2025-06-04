package network

import network.headers.ConnectionHeader
import network.headers.ConnectionValue
import network.headers.ContentTypeHeader
import network.headers.ServerHeader
import serializer.JSONSerializer
import serializer.Serializer
import serializers.TextBodySerializer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class HttpServer(
    private val host: String,
    private var port: Int,
    private val handler: (HttpRequest) -> HttpResponse,
    private val options: HttpServerOptions = HttpServerOptions()
) {
    private val contentSerializers: ContentSerializers = ContentSerializers()
    private var serverSocket: ServerSocket? = null
    private val running = AtomicBoolean(false)

    fun registerJSONSerializer(serializer: JSONSerializer) {
        contentSerializers.registerJSON(serializer)
    }

    fun registerTextSerializer(serializer: TextBodySerializer) {
        contentSerializers.registerText(serializer)
    }

    fun registerHTMLSerializer(serializer: Serializer<String, String, *>) {
        contentSerializers.registerHTML(serializer)
    }

    fun registerFormSerializer(serializer: Serializer<Map<String, String>, String, *>) {
        contentSerializers.registerForm(serializer)
    }

    fun registerMultipartSerializer(serializer: Serializer<ByteArray, String, *>) {
        contentSerializers.registerMultipart(serializer)
    }

    fun start() {
        var attemptCount = 0
        val initialPort = port

        while (attemptCount < options.maxPortAttempts) {
            try {
                serverSocket = ServerSocket()
                serverSocket?.bind(InetSocketAddress(host, port))
                break
            } catch (e: IOException) {
                attemptCount++
                if (attemptCount >= options.maxPortAttempts) {
                    throw IOException(
                        "Failed to bind to any port after ${options.maxPortAttempts} attempts. " +
                                "Initial port attempted: $initialPort. Last error: ${e.message}"
                    )
                }

                println("Port $port is in use, trying next port...")
                port++
                println("Trying on new port: http://$host:$port")
            }
        }

        if (serverSocket == null) {
            throw IOException("Failed to create server socket after ${options.maxPortAttempts} attempts.")
        }

        println("Server is running on http://$host:$port")
        running.set(true)

        while (running.get()) {
            try {
                val clientSocket = serverSocket?.accept() ?: break
                thread {
                    handleClient(clientSocket)
                }
            } catch (e: IOException) {
                if (running.get()) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun close() {
        running.set(false)
        try {
            serverSocket?.close()
            println("Server on port $port has been shut down")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun handleClient(socket: Socket) {
        socket.soTimeout = options.connectionTimeoutMillis.toInt()

        socket.use {
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()
            try {
                // Parse HTTP request properly instead of reading the entire stream
                val requestLine = input.readLine() ?: throw IOException("Empty request")

                // Parse headers
                val headers = mutableMapOf<String, String>()
                var line: String? = null
                var contentLength = 0

                while (input.ready() && input.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                    val parts = line!!.split(":", limit = 2)
                    if (parts.size == 2) {
                        val headerName = parts[0].trim()
                        val headerValue = parts[1].trim()
                        headers[headerName] = headerValue

                        if (headerName.equals("Content-Length", ignoreCase = true)) {
                            contentLength = headerValue.toIntOrNull() ?: 0
                        }
                    }
                }

                // Read body if content length is specified
                val body = if (contentLength > 0) {
                    val bodyChars = CharArray(contentLength)
                    var charsRead = 0
                    var count = 0

                    while (
                        charsRead < contentLength &&
                        input.ready() &&
                        input.read(bodyChars, charsRead, contentLength - charsRead).also { count = it } > 0
                    ) {
                        charsRead += count
                    }

                    String(bodyChars, 0, charsRead)
                } else {
                    ""
                }

                // Reconstruct the raw request for parsing
                val rawRequest = buildString {
                    append(requestLine)
                    append("\r\n")
                    for ((key, value) in headers) {
                        append("$key: $value\r\n")
                    }
                    append("\r\n")
                    append(body)
                }

                val request = HttpRequest.parse(rawRequest)

                if (options.logRequests) {
                    println("[${request.method}] ${request.path}")
                    println("\nRaw request:\n\n$rawRequest")
                }

                val response = handler(request)
                response.headers.add(ServerHeader(options.serverName))
                writeResponse(output, response)
            } catch (e: Exception) {
                e.printStackTrace()
                writeResponse(
                    output, HttpResponse(
                        status = HttpStatus.INTERNAL_SERVER_ERROR,
                        headers = HttpHeaders(
                            ContentTypeHeader(ContentType.TEXT),
                            ServerHeader(options.serverName),
                            ConnectionHeader(ConnectionValue.CLOSE)
                        ),
                        body = "Internal Server Error:\n${e.message}"
                    )
                )
            }
        }
    }

    private fun writeResponse(output: OutputStream, response: HttpResponse) {
        val raw = response.serialize()
        println(raw)
        output.write(raw.toByteArray(Charsets.UTF_8))
        output.flush()
    }
}