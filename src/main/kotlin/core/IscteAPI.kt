package core

import java.net.ServerSocket
import java.net.Socket

import serializer.JSONSerializer
import deserializer.JSONDeserializer
import converter.JSONConverter
import network.HttpRequest
import network.HttpResponse
import java.io.*

class IscteAPI(vararg val names: String) {
    val port: Int = 3000
    val router: Router = Router()
    val jsonSerializer: JSONSerializer = JSONConverter()
    val jsonDeserializer: JSONDeserializer = JSONConverter()

    fun handleClient(socket: Socket) {
        socket.use {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

            try {
                val request = parseRequest(reader)
                val response = router.resolve(request)
                writer.write(response.toRawResponse())
                writer.flush()
            } catch (e: Exception) {
                writer.write(HttpResponse(500, mapOf("Content-Type" to "text/plain"), "Internal Server Error").toRawResponse())
                writer.flush()
            }
        }
    }

    fun start(port: Int = this.port) {
        var server: ServerSocket
        while(true){
            try {
                server = ServerSocket(port)
                break
            } catch(e: IOException){
                println("Error while starting service on port $port ")
            }
        }

        println("Server started on port $port")

        while (true) {
            val client = server.accept()
            Thread {
                handleClient(client)
            }.start()
        }
    }
}