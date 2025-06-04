import network.HttpResponse
import network.HttpServer

fun main() {
    val server = HttpServer(
        "127.0.0.1", 5002,
        { req ->
            HttpResponse()
        }
    )
    server.start()
}