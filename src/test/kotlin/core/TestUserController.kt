package core

import network.HttpResponse
import network.headers.ContentTypeHeader
import network.ContentType

@Controller("/api/users")
class TestUserController {
    private val users = mutableMapOf<String, User>()
    
    init {
        users["1"] = User("1", "John Doe", "john@example.com")
        users["2"] = User("2", "Jane Smith", "jane@example.com")
    }
    
    @Get("/")
    fun getAllUsers(): List<User> {
        return users.values.toList()
    }
    
    @Get("/{id}")
    fun getUser(@Param("id") userId: String): User? {
        return users[userId]
    }
    
    @Post("/")
    fun createUser(@Body body: String): User {
        // In a real application, you'd parse the JSON here
        // For this test, we'll create a simple user
        val newId = (users.size + 1).toString()
        val newUser = User(newId, "User $newId", "user$newId@example.com")
        users[newId] = newUser
        return newUser
    }
    
    @Put("/{id}")
    fun updateUser(@Param("id") userId: String, @Body body: String): User? {
        return users[userId]?.also { user ->
            users[userId] = User(user.id, "Updated ${user.name}", user.email)
        }
    }
    
    @Delete("/{id}")
    fun deleteUser(@Param("id") userId: String): HttpResponse {
        return if (users.remove(userId) != null) {
            val response = HttpResponse.ok("User deleted successfully")
            response
        } else {
            HttpResponse.notFound("User not found")
        }
    }
}

// Data class for a User
data class User(val id: String, val name: String, val email: String)