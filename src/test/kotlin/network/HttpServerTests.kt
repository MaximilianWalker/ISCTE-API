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
                Thread.sleep(100) // Simulate some processing time
                HttpResponse.ok("Hello from concurrent test")
            }
        )

        thread {
            server.start()
        }

        Thread.sleep(200) // Wait for server to start

        val results = mutableListOf<String?>()
        val exceptions = mutableListOf<Exception>()
        val threads = mutableListOf<Thread>()

        // Create concurrent client threads
        repeat(10) { index ->
            val clientThread = thread {
                try {
                    val client = Socket("127.0.0.1", 5004)
                    val writer = PrintWriter(client.getOutputStream(), true)
                    val reader = BufferedReader(InputStreamReader(client.getInputStream()))

                    writer.println("GET /test$index HTTP/1.1")
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

                    synchronized(results) {
                        results.add(body)
                    }

                    // Verify response in thread
                    assertTrue(response[0].contains("200"))
                    assertTrue(body?.contains("Hello from concurrent test") == true)

                    client.close()
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                }
            }
            threads.add(clientThread)
        }

        // Wait for all threads to complete
        threads.forEach { it.join() }

        // Assert no exceptions occurred
        if (exceptions.isNotEmpty()) {
            throw AssertionError("Concurrent test failed with exceptions: ${exceptions.map { it.message }}")
        }

        // Assert all requests were handled
        assertEquals(10, results.size)
        assertTrue(results.all { it?.contains("Hello from concurrent test") == true })

        server.close()
    }
}