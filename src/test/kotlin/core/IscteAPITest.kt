package core

import network.HttpRequest
import network.HttpHeaders
import network.HttpResponse
import network.ContentType
import network.headers.ContentTypeHeader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.reflect.KClass

class IscteAPITest {
    private lateinit var api: IscteAPI
    private val port = 3456 // Using a different port for tests
    
    @BeforeEach
    fun setUp() {
        api = IscteAPI(port = port)
        api.registerController(TestUserController::class)
        
        // Start the API in a separate thread
        Thread {
            api.start()
        }.start()
        
        // Wait for server to start
        Thread.sleep(500)
    }
    
    @AfterEach
    fun tearDown() {
        // Cleanup code if needed
    }
    
    @Test
    fun `test controller registration`() {
        // This is testing internal functionality - in a real test you might
        // verify routes are registered by making requests to them
        val router = api::class.java.getDeclaredField("router").apply { isAccessible = true }.get(api) as Router
        
        // Create a simple request to check if the route exists
        val request = HttpRequest("GET", "/api/users", HttpHeaders(), null)
        val response = router::class.java.getDeclaredMethod("resolve", HttpRequest::class.java)
            .apply { isAccessible = true }
            .invoke(router, request) as HttpResponse
            
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test get all users`() {
        val request = HttpRequest("GET", "/api/users", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test get user by id`() {
        val request = HttpRequest("GET", "/api/users/1", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test create new user`() {
        val headers = HttpHeaders()
        headers.add(ContentTypeHeader(ContentType.JSON))
        
        val request = HttpRequest("POST", "/api/users", headers, """{"name": "New User", "email": "new@example.com"}""")
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test update user`() {
        val headers = HttpHeaders()
        headers.add(ContentTypeHeader(ContentType.JSON))
        
        val request = HttpRequest("PUT", "/api/users/1", headers, """{"name": "Updated Name", "email": "updated@example.com"}""")
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test delete user`() {
        val request = HttpRequest("DELETE", "/api/users/1", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
        assertTrue(response.body.toString().contains("deleted"))
    }
    
    @Test
    fun `test delete non-existent user`() {
        val request = HttpRequest("DELETE", "/api/users/999", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(404, response.status.code)
    }
    
    @Test
    fun `test route not found`() {
        val request = HttpRequest("GET", "/api/invalid", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(404, response.status.code)
    }
}