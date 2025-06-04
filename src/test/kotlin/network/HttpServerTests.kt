package network

import java.io.*
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpServerTests {

    @Test
    fun `server starts and listens on specified port`() {
        val server = HttpServer(
            "127.0.0.1", 5001,
            { req ->
                HttpResponse.ok("Hello World")
            }
        )

        thread {
            server.start()
        }

        Thread.sleep(200) // Wait for server to start

        val client = Socket("127.0.0.1", 5001)
        assertTrue(client.isConnected)
        client.close()
        server.close()
    }

    @Test
    fun `server handles valid HTTP request`() {
        val server = HttpServer(
            "127.0.0.1", 5002,
            { req ->
                assertEquals(HttpMethod.GET, req.method)
                assertEquals("/test", req.path)
                HttpResponse.ok("Request handled")
            }
        )

        thread {
            server.start()
        }

        Thread.sleep(200) // Wait for server to start

        val client = Socket("127.0.0.1", 5002)
        val writer = PrintWriter(client.getOutputStream(), true)
        val reader = BufferedReader(InputStreamReader(client.getInputStream()))

        writer.println("GET /test HTTP/1.1")
        writer.println("Host: 127.0.0.1")
        writer.println("Content-Type: text/plain")
        writer.println("Connection: close")
        writer.println() // End of headers

        val response = mutableListOf<String>()
        var line: String? = reader.readLine()
        while (line != null) {
            response.add(line)
            if (line.isEmpty()) break // Stop at end of headers
            line = reader.readLine()
        }
        val body = reader.readLine()

        assertTrue(response[0].contains("200"))
        assertEquals("Request handled", body)

        client.close()
        server.close()
    }

    @Test
    fun `server handles invalid HTTP request with error response`() {
        val server = HttpServer(
            "127.0.0.1", 5003,
            { req ->
                throw IllegalArgumentException("Invalid request")
            }
        )

        thread {
            server.start()
        }

        Thread.sleep(200) // Wait for server to start

        val client = Socket("127.0.0.1", 5003)
        val writer = PrintWriter(client.getOutputStream(), true)
        val reader = BufferedReader(InputStreamReader(client.getInputStream()))

        writer.println("INVALID /test HTTP/1.1")
        writer.println("Host: 127.0.0.1")
        writer.println() // End of headers

        val response = mutableListOf<String>()
        var line: String? = reader.readLine()
        while (line != null) {
            response.add(line)
            if (line.isEmpty()) break // Stop at end of headers
            line = reader.readLine()
        }
        val body = reader.readLine()

        assertTrue(response[0].contains("500"))
        println(response)

        client.close()
        server.close()
    }

    @Test
    fun `server handles multiple concurrent connections`() {
        val server = HttpServer(
            "127.0.0.1", 5004,
            { req ->
                HttpResponse.ok("Hello from concurrent test")
            }
        )

        thread {
            server.start()
        }

        Thread.sleep(200) // Wait for server to start

        val clients = (1..10).map {
            Socket("127.0.0.1", 5004)
        }

        clients.forEach { client ->
            thread {
                val writer = PrintWriter(client.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))

                writer.println("GET / HTTP/1.1")
                writer.println("Host: 127.0.0.1")
                writer.println("Content-Type: text/plain")
                writer.println("Connection: close")
                writer.println() // End of headers

                val response = mutableListOf<String>()
                var line: String? = reader.readLine()
                while (line != null) {
                    response.add(line)
                    if (line.isEmpty()) break // Stop at end of headers
                    line = reader.readLine()
                }
                val body = reader.readLine()

                println(response)
                println(body)
                assertTrue(response[0].contains("200"))
                assertTrue(body?.contains("Hello from concurrent test") == true)

                client.close()
            }
        }
        server.close()
    }
}