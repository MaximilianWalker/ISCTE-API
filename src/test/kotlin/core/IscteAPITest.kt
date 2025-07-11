package core

import network.*
import network.headers.ContentTypeHeader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.reflect.KClass

class IscteAPITest {
    private lateinit var api: IscteAPI
    private val port = 5200 // Using a different port for tests
    
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
        val request = HttpRequest(HttpMethod.GET, "/api/users", HttpHeaders(), null)
        val response = router::class.java.getDeclaredMethod("resolve", HttpRequest::class.java)
            .apply { isAccessible = true }
            .invoke(router, request) as HttpResponse
            
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test get all users`() {
        val request = HttpRequest(HttpMethod.GET, "/api/users", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test get user by id`() {
        val request = HttpRequest(HttpMethod.GET, "/api/users/1", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test create new user`() {
        val headers = HttpHeaders()
        headers.add(ContentTypeHeader(ContentType.JSON))

        val jsonObject = JSONObject(
            "name" to JSONString("New User"),
            "email" to JSONString("new@example.com"),
        )
        
        val request = HttpRequest(HttpMethod.POST, "/api/users", headers, jsonObject)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test update user`() {
        val headers = HttpHeaders()
        headers.add(ContentTypeHeader(ContentType.JSON))

        val jsonObject = JSONObject(
            "name" to JSONString("Updated Name"),
            "email" to JSONString("updated@example.com"),
        )

        val request = HttpRequest(HttpMethod.PUT, "/api/users/1", headers, jsonObject)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
    }
    
    @Test
    fun `test delete user`() {
        val request = HttpRequest(HttpMethod.DELETE, "/api/users/1", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(200, response.status.code)
        assertTrue(response.body.toString().contains("deleted"))
    }
    
    @Test
    fun `test delete non-existent user`() {
        val request = HttpRequest(HttpMethod.DELETE, "/api/users/999", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(404, response.status.code)
    }
    
    @Test
    fun `test route not found`() {
        val request = HttpRequest(HttpMethod.GET, "/api/invalid", HttpHeaders(), null)
        val response = api.handleRequest(request)
        
        assertEquals(404, response.status.code)
    }
}