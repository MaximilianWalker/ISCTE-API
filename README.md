# ISCTE-API Framework

A lightweight Kotlin HTTP server framework with annotation-based routing and controller support.

## Features

- 🚀 Lightweight HTTP server implementation
- 📝 Annotation-based routing (`@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`)
- 🎯 Controller-based architecture with `@Controller`
- 🔗 Path parameters with `@Param`
- 🔍 Query parameters with `@Query`
- 📦 Request body handling with `@Body`
- 🔄 JSON serialization/deserialization powered by [ISCTE-JSON][iscte-json-link]
- 🛠️ Middleware support
- ⚡ Concurrent request handling
- 🔧 Configurable server options

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven for dependency management
- Kotlin 2.1.10+

### Installation

Clone the repository and add it to your project:

```bash
git clone <repository-url>
cd ISCTE-API
mvn clean install
```

## Basic Usage

### 1. Creating a Simple Controller

Create a controller class annotated with `@Controller`:

```kotlin
@Controller("/api/users")
class UserController {
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
            HttpResponse.ok("User deleted successfully")
        } else {
            HttpResponse.notFound("User not found")
        }
    }
}

data class User(val id: String, val name: String, val email: String)
```

### 2. Setting up the API Server

```kotlin
fun main() {
    val api = IscteAPI(
        host = "localhost",
        port = 3000
    )
    
    // Register your controllers
    api.registerController(UserController::class)
    
    // Start the server
    api.start()
}
```

## Annotations Reference

### Class Annotations

#### `@Controller`
Marks a class as a controller and defines the base path for all routes in the controller.

```kotlin
@Controller("/api/users")  // Base path
class UserController {
    // Controller methods...
}
```

### Method Annotations

#### HTTP Method Annotations
- `@Get(vararg path: String)` - Handle GET requests
- `@Post(vararg path: String)` - Handle POST requests  
- `@Put(vararg path: String)` - Handle PUT requests
- `@Delete(vararg path: String)` - Handle DELETE requests
- `@Patch(vararg path: String)` - Handle PATCH requests

```kotlin
@Get("/", "/list")  // Multiple paths supported
fun getUsers(): List<User> { ... }

@Get("/{id}")  // Path parameters with curly braces
fun getUser(@Param("id") id: String): User? { ... }
```

### Parameter Annotations

#### `@Param`
Extract path parameters from the URL.

```kotlin
@Get("/{userId}/posts/{postId}")
fun getUserPost(
    @Param("userId") userId: String,
    @Param("postId") postId: String
): Post? { ... }
```

#### `@Query`
Extract query parameters from the URL.

```kotlin
@Get("/search")
fun searchUsers(
    @Query("name") name: String,
    @Query("limit") limit: String?
): List<User> { ... }
```

#### `@Body`
Access the request body.

```kotlin
@Post("/")
fun createUser(@Body body: String): User { ... }
```

## Advanced Features

### Custom HTTP Responses

You can return custom HTTP responses with specific status codes:

```kotlin
@Get("/{id}")
fun getUser(@Param("id") id: String): HttpResponse {
    val user = findUser(id)
    return if (user != null) {
        HttpResponse.ok(user)
    } else {
        HttpResponse.notFound("User not found")
    }
}
```

Available response helpers:
- `HttpResponse.ok(body)` - 200 OK
- `HttpResponse.badRequest(body)` - 400 Bad Request
- `HttpResponse.notFound(body)` - 404 Not Found
- `HttpResponse.internalServerError(body)` - 500 Internal Server Error

### Middleware Support

Add middleware to process requests before they reach your controllers:

```kotlin
val api = IscteAPI()

// Add logging middleware
api.addMiddleware { request ->
    println("${request.method} ${request.path}")
    request
}

// Add authentication middleware
api.addMiddleware { request ->
    // Add authentication logic here
    request
}
```

### Automatic JSON Serialization

The framework automatically converts return values to JSON using the custom [ISCTE-JSON][iscte-json-link] library:

```kotlin
@Get("/users")
fun getUsers(): List<User> {
    // This automatically returns JSON with Content-Type: application/json
    // using the ISCTE-JSON library for serialization
    return listOf(
        User("1", "John", "john@example.com"),
        User("2", "Jane", "jane@example.com")
    )
}
```

### Multiple Response Types

Your controller methods can return different types:

```kotlin
@Get("/info")
fun getInfo(): String {
    return "Server is running"  // Returns plain text
}

@Get("/status")
fun getStatus(): HttpResponse {
    return HttpResponse.ok("All systems operational")  // Custom response
}

@Get("/data")
fun getData(): Map<String, Any> {
    return mapOf("status" to "success", "data" to listOf(1, 2, 3))  // JSON response
}
```

### Type Conversion

Path and query parameters are automatically converted to appropriate types:

```kotlin
@Get("/user/{id}/age/{age}")
fun updateUserAge(
    @Param("id") userId: String,      // String parameter
    @Param("age") age: Int,           // Automatically converted to Int
    @Query("active") active: Boolean   // Automatically converted to Boolean
): User { ... }
```

Supported conversions:
- `String` - No conversion
- `Int` - `String.toIntOrNull()`
- `Long` - `String.toLongOrNull()`
- `Double` - `String.toDoubleOrNull()`
- `Float` - `String.toFloatOrNull()`
- `Boolean` - `String.toBoolean()`

### Request Injection

You can inject the entire `HttpRequest` object into your methods:

```kotlin
@Post("/upload")
fun handleUpload(request: HttpRequest): HttpResponse {
    // Access headers, body, query params, etc.
    val contentType = request.headers.find { it.name == "Content-Type" }
    return HttpResponse.ok("Received ${request.body.length} bytes")
}
```

## Configuration

### Server Configuration

```kotlin
val api = IscteAPI(
    host = "0.0.0.0",  // Bind to all interfaces
    port = 8080        // Custom port
)
```

### Path Normalization

The framework automatically normalizes paths:
- Multiple slashes are collapsed: `//api///users` → `/api/users`
- Trailing slashes are removed: `/api/users/` → `/api/users`
- Empty paths become root: `` → `/`

## Testing

### Integration Testing Example

```kotlin
@Test
fun `test GET all users`() {
    val api = IscteAPI(port = 5100)
    api.registerController(TestUserController::class)
    
    // Start server in background
    Thread { api.start() }.start()
    Thread.sleep(500) // Wait for startup
    
    val url = URL("http://localhost:5100/api/users")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    
    assertEquals(200, connection.responseCode)
    val response = connection.inputStream.bufferedReader().use { it.readText() }
    assertTrue(response.contains("John Doe"))
}
```

## Error Handling

The framework provides built-in error handling:

- **Parameter extraction errors** → 400 Bad Request
- **Method execution errors** → 500 Internal Server Error
- **Route not found** → 404 Not Found

### Custom Error Handling

```kotlin
@Get("/{id}")
fun getUser(@Param("id") id: String): HttpResponse {
    return try {
        val user = userService.findById(id)
        HttpResponse.ok(user)
    } catch (e: UserNotFoundException) {
        HttpResponse.notFound("User with id $id not found")
    } catch (e: Exception) {
        HttpResponse.internalServerError("An error occurred: ${e.message}")
    }
}
```

## Custom HTTP Server Implementation

The ISCTE-API framework is built on top of a custom HTTP server implementation that requires **zero external dependencies**. The server is written entirely in Kotlin using only the Java Standard Library, making it lightweight and self-contained.

### Key Features of the HTTP Server

- **Pure Kotlin/Java implementation** - No external HTTP libraries required
- **Concurrent request handling** - Each request is handled in its own thread
- **Configurable options** - Timeout, connection limits, logging, etc.
- **Content serialization support** - JSON, Text, HTML, Form, and Multipart
- **Automatic port selection** - If the specified port is busy, it tries the next available port

### Using the HTTP Server Directly

If you prefer to use the low-level HTTP server without the framework's annotations and controllers, you can do so:

```kotlin
import network.*

fun main() {
    // Create server options (optional)
    val options = HttpServerOptions(
        maxConnections = 100,
        connectionTimeoutMillis = 30_000,
        logRequests = true,
        serverName = "MyCustomServer"
    )
    
    // Create the HTTP server with a request handler
    val server = HttpServer(
        host = "localhost",
        port = 8080,
        handler = { request: HttpRequest ->
            when {
                request.method == HttpMethod.GET && request.path == "/hello" -> {
                    HttpResponse.ok("Hello from custom server!")
                }
                request.method == HttpMethod.GET && request.path.startsWith("/user/") -> {
                    val userId = request.path.removePrefix("/user/")
                    HttpResponse.ok("User ID: $userId")
                }
                request.method == HttpMethod.POST && request.path == "/echo" -> {
                    HttpResponse.ok("Echo: ${request.body}")
                }
                else -> {
                    HttpResponse.notFound("Route not found")
                }
            }
        },
        options = options
    )
    
    // Start the server
    server.start()
}
```

### Server Configuration Options

You can customize the server behavior with `HttpServerOptions`:

```kotlin
val options = HttpServerOptions(
    maxConnections = 200,           // Maximum concurrent connections
    connectionTimeoutMillis = 60_000, // Connection timeout in milliseconds
    logRequests = true,             // Enable request logging
    enableKeepAlive = false,        // Enable/disable HTTP keep-alive
    serverName = "ISCTE-Server",    // Server name in response headers
    maxPortAttempts = 5             // Max attempts to find available port
)
```

### Custom Serializers

The HTTP server supports custom serializers for different content types:

```kotlin
val server = HttpServer("localhost", 8080, handler)

// Register custom JSON serializer
server.registerJSONSerializer(MyCustomJSONSerializer())

// Register custom text serializer
server.registerTextSerializer(MyCustomTextSerializer())

// Register custom HTML serializer
server.registerHTMLSerializer(MyCustomHTMLSerializer())

// Register custom form serializer
server.registerFormSerializer(MyCustomFormSerializer())

// Register custom multipart serializer
server.registerMultipartSerializer(MyCustomMultipartSerializer())
```

### Working with Requests and Responses

The HTTP server provides rich request and response objects:

```kotlin
val handler = { request: HttpRequest ->
    // Access request properties
    println("Method: ${request.method}")
    println("Path: ${request.path}")
    println("Query params: ${request.queryParams}")
    println("Headers: ${request.headers}")
    println("Body: ${request.body}")
    
    // Create custom responses
    val response = when (request.path) {
        "/json" -> {
            val jsonResponse = HttpResponse.ok("""{"message": "Hello JSON"}""")
            jsonResponse.headers.add(ContentTypeHeader(ContentType.JSON))
            jsonResponse
        }
        "/html" -> {
            val htmlResponse = HttpResponse.ok("<h1>Hello HTML</h1>")
            htmlResponse.headers.add(ContentTypeHeader(ContentType.HTML))
            htmlResponse
        }
        else -> HttpResponse.ok("Hello World")
    }
    
    response
}
```

## API Reference

### Core Classes

- `IscteAPI` - Main API class for setting up the framework
- `HttpServer` - Low-level HTTP server implementation with zero dependencies
- `Router` - Request routing logic
- `HttpRequest` - HTTP request representation
- `HttpResponse` - HTTP response representation
- `HttpServerOptions` - Configuration options for the HTTP server

### HTTP Methods

All standard HTTP methods are supported:
- GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE, CONNECT

### Content Types

The framework supports various content types:
- **JSON** (`application/json`) - Automatic serialization/deserialization using [ISCTE-JSON][iscte-json-link]
- **Text** (`text/plain`) - Plain text content
- **HTML** (`text/html`) - HTML content
- **Form** (`application/x-www-form-urlencoded`) - Form data

## Building and Running

### Build the project:
```bash
mvn clean compile
```

### Run tests:
```bash
mvn test
```

### Package the project:
```bash
mvn package
```

### Run the example:
```bash
mvn exec:java -Dexec.mainClass="MainKt"
```

## Complete Example

Here's a complete working example:

```kotlin
import core.*
import network.*

@Controller("/api")
class ExampleController {
    
    @Get("/hello")
    fun hello(): String = "Hello, World!"
    
    @Get("/hello/{name}")
    fun helloName(@Param("name") name: String): String = "Hello, $name!"
    
    @Get("/search")
    fun search(@Query("q") query: String): Map<String, Any> {
        return mapOf(
            "query" to query,
            "results" to listOf("result1", "result2")
        )
    }
    
    @Post("/data")
    fun postData(@Body body: String): HttpResponse {
        return HttpResponse.ok("Received: $body")
    }
}

fun main() {
    val api = IscteAPI(port = 8080)
    api.registerController(ExampleController::class)
    
    println("Starting ISCTE-API server...")
    api.start()
}
```

Test the endpoints:
```bash
curl http://localhost:8080/api/hello
curl http://localhost:8080/api/hello/John
curl "http://localhost:8080/api/search?q=kotlin"
curl -X POST -d "test data" http://localhost:8080/api/data
```

## License

This project is licensed under the MIT License.

[iscte-json-link]: https://github.com/MaximilianWalker/ISCTE-JSON