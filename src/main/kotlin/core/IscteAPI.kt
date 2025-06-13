package core

import inference.JSONInference
import network.HttpMethod
import network.HttpRequest
import network.HttpResponse
import network.HttpServer
import network.headers.ContentTypeHeader
import network.ContentType
import kotlin.reflect.*
import kotlin.reflect.full.*

class IscteAPI(
    private val host: String = "localhost",
    private val port: Int = 3000
) {
    private val server: HttpServer = HttpServer(
        host,
        port,
        { request -> handleRequest(request) }
    )
    private val router: Router = Router()
    private val controllers = mutableListOf<Any>()
    private val middlewares = mutableListOf<(HttpRequest) -> HttpRequest>()

    fun registerController(controllerClass: KClass<*>) {
        if (controllerClass.findAnnotation<Controller>() == null) {
            throw IllegalArgumentException("Class ${controllerClass.qualifiedName} is not annotated with @Controller")
        }

        println("Registering controller: ${controllerClass.qualifiedName}")

        val controllerInstance = controllerClass.createInstance()
        controllers.add(controllerInstance)

        val controllerPath = controllerClass.findAnnotation<Controller>()?.path?.firstOrNull() ?: ""
        registerControllerMethods(controllerInstance, controllerClass, controllerPath)
    }

    private fun registerControllerMethods(controllerInstance: Any, controllerClass: KClass<*>, controllerPath: String) {
        val methods = controllerClass.memberFunctions

        methods.forEach { method ->
            // Look for HTTP method annotations
            processMethodAnnotation<Get>(method, controllerInstance, controllerPath, HttpMethod.GET)
            processMethodAnnotation<Post>(method, controllerInstance, controllerPath, HttpMethod.POST)
            processMethodAnnotation<Put>(method, controllerInstance, controllerPath, HttpMethod.PUT)
            processMethodAnnotation<Patch>(method, controllerInstance, controllerPath, HttpMethod.PATCH)
            processMethodAnnotation<Delete>(method, controllerInstance, controllerPath, HttpMethod.DELETE)
        }
    }

    private inline fun <reified T : Annotation> processMethodAnnotation(
        method: KFunction<*>,
        controllerInstance: Any,
        controllerPath: String,
        httpMethod: HttpMethod
    ) {
        val annotation = method.findAnnotation<T>() ?: return

        // Get the path from the annotation
        val paths = when (annotation) {
            is Get -> annotation.path
            is Post -> annotation.path
            is Put -> annotation.path
            is Patch -> annotation.path
            is Delete -> annotation.path
            else -> emptyArray()
        }

        // If no paths specified, use an empty string (root path)
        if (paths.isEmpty()) {
            registerRoute(controllerInstance, method, controllerPath, "", httpMethod)
            return
        }

        // Register each path
        paths.forEach { path ->
            registerRoute(controllerInstance, method, controllerPath, path, httpMethod)
        }
    }

    private fun registerRoute(
        controllerInstance: Any,
        method: KFunction<*>,
        controllerPath: String,
        methodPath: String,
        httpMethod: HttpMethod
    ) {
        val fullPath = normalizePath("$controllerPath/$methodPath")

        println("Registering route: ${httpMethod.name} $fullPath")

        val handler = handler@{ request: HttpRequest ->
            var args: Map<KParameter, Any?> = emptyMap()
            try {
                args = extractParameters(method, fullPath, request, controllerInstance)
            } catch (e: Exception) {
                e.printStackTrace()
                return@handler HttpResponse.badRequest("Invalid parameters: ${e.message}")
            }

            args.entries.forEach {
                println(it.key)
                println(it.value)
            }
            try {
                val result = method.callBy(args)

                when (result) {
                    is HttpResponse -> result
                    is String -> HttpResponse.ok(result)
                    is Unit -> HttpResponse.ok("")
                    null -> HttpResponse.ok("")
                    else -> {
                        // Create a JSON response for objects
                        val jsonResult = JSONInference.convertFrom(result)
                        val response = HttpResponse.ok(jsonResult)
                        response.headers.add(ContentTypeHeader(ContentType.JSON))
                        response
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                HttpResponse.internalServerError("Error processing request: ${e.message}")
            }
        }

        when (httpMethod) {
            HttpMethod.GET -> router.get(fullPath, handler)
            HttpMethod.POST -> router.post(fullPath, handler)
            HttpMethod.PUT -> router.put(fullPath, handler)
            HttpMethod.DELETE -> router.delete(fullPath, handler)
            HttpMethod.PATCH -> router.patch(fullPath, handler)
            else -> throw UnsupportedOperationException("HTTP method $httpMethod not supported")
        }
    }

    private fun normalizePath(path: String): String {
        return path.replace(Regex("/+"), "/").removeSuffix("/").ifEmpty { "/" }
    }

    private fun extractParameters(
        method: KFunction<*>,
        path: String,
        request: HttpRequest,
        instance: Any
    ): Map<KParameter, Any?> {
        val args = mutableMapOf<KParameter, Any?>()
        val pathParameters = extractPathParameters(path, request.path)
        val kek = request.path
        println("printing paths: \n $path \n $kek")

        println("printing path params")
        pathParameters.entries.forEach{
            println(it.key + " - > " + it.value)
        }

        // Add the instance parameter first
        method.parameters.find { it.kind == KParameter.Kind.INSTANCE }?.let {
            args[it] = instance
        }

        // Process remaining parameters
        method.parameters.filter { it.kind != KParameter.Kind.INSTANCE }.forEach { parameter ->
            val paramAnnotation = parameter.findAnnotation<Param>()
            val queryAnnotation = parameter.findAnnotation<Query>()
            val bodyAnnotation = parameter.findAnnotation<Body>()

            when {
                // Path parameter
                paramAnnotation != null -> {
                    println("found param annotation")
                    val paramName = paramAnnotation.path.firstOrNull() ?: parameter.name ?: ""
                    val paramValue = pathParameters[paramName]

                    if (paramValue != null) {
                        args[parameter] = convertParameterValue(paramValue, parameter.type)
                    }
                }

                // Query parameter
                queryAnnotation != null -> {
                    println("found query annotation")
                    val queryName = queryAnnotation.path.firstOrNull() ?: parameter.name ?: ""
                    val queryValue = request.queryParams[queryName]

                    if (queryValue != null) {
                        args[parameter] = convertParameterValue(queryValue, parameter.type)
                    }
                }

                // Body parameter
                bodyAnnotation != null -> {
                    println("found body annotation")
                    // Pass the request body to this parameter
                    args[parameter] = request.body
                }

                // If no annotation, try to inject based on type
                else -> {
                    when (parameter.type.classifier) {
                        HttpRequest::class -> args[parameter] = request
                        // Add more default injections as needed
                    }
                }
            }
        }

        return args
    }

    private fun extractPathParameters(routePath: String, path: String): Map<String, String> {
        val routeSegments = routePath.trim('/').split('/')
        val pathSegments = path.trim('/').split('/')

        if (pathSegments.size != routeSegments.size) {
            throw IllegalArgumentException("Path segments do not match route segments: $routePath vs $path")
        }

        return routeSegments.zip(pathSegments)
            .mapNotNull { (routeSeg, pathSeg) ->
                if (routeSeg.startsWith("{") && routeSeg.endsWith("}")) {
                    val name = routeSeg.substring(1, routeSeg.length - 1)
                    name to pathSeg
                } else null
            }
            .toMap()
    }

    private fun convertParameterValue(value: String, type: KType): Any? {
        return when (type.classifier) {
            String::class -> value
            Int::class -> value.toIntOrNull()
            Long::class -> value.toLongOrNull()
            Double::class -> value.toDoubleOrNull()
            Boolean::class -> value.toBoolean()
            Float::class -> value.toFloatOrNull()
            else -> value
        }
    }

    fun addMiddleware(middleware: (HttpRequest) -> HttpRequest) {
        middlewares.add(middleware)
    }

    fun handleRequest(request: HttpRequest): HttpResponse {
        // Apply all middlewares in order
        var currentRequest = request
        for (middleware in middlewares) {
            currentRequest = middleware(currentRequest)
        }

        // Use the router to resolve the request
        return router.resolve(currentRequest)
    }

    fun start(port: Int = this.port) {
        println("Starting server with ${controllers.size} registered controllers")
        server.start()
        println("Server started on http://$host:$port")
    }
}