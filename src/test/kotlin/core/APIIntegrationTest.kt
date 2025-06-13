package core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Integration tests for the ISCTE-API framework.
 * These tests start an actual server and make HTTP requests to test the API.
 */
class APIIntegrationTest {
    private lateinit var api: IscteAPI
    private val port = 5100 // Use a unique port for these integration tests
    private val baseUrl = "http://localhost:$port"
    private var serverThread: Thread? = null
    
    @BeforeEach
    fun setUp() {
        api = IscteAPI(port = port)
        api.registerController(TestUserController::class)
        
        // Start server in a background thread
        serverThread = Thread {
            api.start()
        }.apply {
            isDaemon = true
            start()
        }
        
        // Wait for server to start
        Thread.sleep(500)
    }
    
    @AfterEach
    fun tearDown() {
        // Cleanup
        serverThread?.interrupt()
        Thread.sleep(100) // Give the server time to shutdown
    }
    
    @Test
    fun `test GET all users`() {
        val url = URL("$baseUrl/api/users")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        val responseCode = connection.responseCode
        assertEquals(200, responseCode)
        
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        assertTrue(response.contains("John Doe"))
        assertTrue(response.contains("jane@example.com"))
        
        connection.disconnect()
    }
    
    @Test
    fun `test GET specific user`() {
        val url = URL("$baseUrl/api/users/1")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        val responseCode = connection.responseCode
        assertEquals(200, responseCode)
        
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        assertTrue(response.contains("John Doe"))
        
        connection.disconnect()
    }
    
    @Test
    fun `test POST new user`() {
        val url = URL("$baseUrl/api/users")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        
        val jsonInput = """{"name":"New User","email":"new@example.com"}"""
        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(jsonInput)
        writer.flush()
        writer.close()
        
        val responseCode = connection.responseCode
        assertEquals(200, responseCode)
        
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        assertTrue(response.contains("User"))
        
        connection.disconnect()
    }
    
    @Test
    fun `test PUT update user`() {
        val url = URL("$baseUrl/api/users/1")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        
        val jsonInput = """{"name":"Updated Name","email":"updated@example.com"}"""
        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(jsonInput)
        writer.flush()
        writer.close()
        
        val responseCode = connection.responseCode
        assertEquals(200, responseCode)
        
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        assertTrue(response.contains("Updated"))
        
        connection.disconnect()
    }
    
    @Test
    fun `test DELETE user`() {
        val url = URL("$baseUrl/api/users/2")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        
        val responseCode = connection.responseCode
        assertEquals(200, responseCode)
        
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        assertTrue(response.contains("deleted"))
        
        connection.disconnect()
    }
    
    @Test
    fun `test DELETE non-existent user returns 404`() {
        val url = URL("$baseUrl/api/users/999")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        
        val responseCode = connection.responseCode
        assertEquals(404, responseCode)
        
        connection.disconnect()
    }
    
    @Test
    fun `test invalid route returns 404`() {
        val url = URL("$baseUrl/api/nonexistent")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        val responseCode = connection.responseCode
        assertEquals(404, responseCode)
        
        connection.disconnect()
    }
    
    // Helper method to make HTTP requests
    private fun sendRequest(
        method: String, 
        endpoint: String, 
        body: String? = null,
        contentType: String = "application/json"
    ): Pair<Int, String> {
        val url = URL("$baseUrl$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", contentType)
            
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(body)
            writer.flush()
            writer.close()
        }
        
        val responseCode = connection.responseCode
        val responseBody = try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }
        
        connection.disconnect()
        return responseCode to responseBody
    }
}