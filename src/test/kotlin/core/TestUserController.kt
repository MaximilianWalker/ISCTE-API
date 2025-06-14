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
    fun createUser(@Body user: User): User {
        val newId = (users.size + 1).toString()
        val newUser = User(newId, user.name, user.email)
        users[newId] = newUser
        return newUser
    }

    @Put("/{id}")
    fun updateUser(@Param("id") userId: String, @Body user: User): User? {
        return if (users.containsKey(userId)) {
            val updatedUser = User(userId, user.name, user.email)
            users[userId] = updatedUser
            updatedUser
        } else {
            null
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
data class User(val id: String?, val name: String, val email: String)