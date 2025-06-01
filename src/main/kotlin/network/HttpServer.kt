package network

import serializer.Serializer
import kotlin.concurrent.thread

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class HttpServer(
    private val host: String,
    private val port: Int,
    private val handler: (HttpRequest) -> HttpResponse
) {
    private val contentSerializers: ContentSerializers = ContentSerializers()

    fun registerSerializer(contentType: ContentType, serializer: Serializer<*, *, *>){
        contentSerializers.register(contentType, serializer)
    }

    fun start() {
        val serverSocket = ServerSocket()
        serverSocket.bind(InetSocketAddress(host, port))
        println("Server is running on http://$host:$port")

        while (true) {
            val client = serverSocket.accept()
            thread {
                handleClient(client)
            }
        }
    }

    private fun handleClient(socket: Socket) {
        socket.use {
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()

            try {
                val request = HttpRequest.parse(input)
                val response = handler(request)
                writeResponse(output, response)
            } catch (e: Exception) {
                writeResponse(output, HttpResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR,
                    headers = mapOf("Content-Type" to ContentType.TEXT.toString()),
                    body = "Internal Server Error:\n${e.message}"
                ))
            }
        }
    }

    private fun writeResponse(output: OutputStream, response: HttpResponse) {
        val raw = response.toRawResponse()
        output.write(raw.toByteArray(Charsets.UTF_8))
        output.flush()
    }
}