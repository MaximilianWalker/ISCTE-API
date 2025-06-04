package core

import network.HttpMethod
import network.HttpRequest
import network.HttpResponse

class Router {
    // Using a more complex structure for routes to support path parameters
    private data class RouteEntry(
        val method: HttpMethod,
        val pathMatcher: PathMatcher,
        val handler: (HttpRequest, Map<String, String>) -> HttpResponse
    )
    
    private val routes = mutableListOf<RouteEntry>()

    fun get(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.GET, path, { req, params -> handler(req.copy(pathParams = params)) })
    }

    fun post(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.POST, path, { req, params -> handler(req.copy(pathParams = params)) })
    }
    
    fun put(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.PUT, path, { req, params -> handler(req.copy(pathParams = params)) })
    }
    
    fun delete(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.DELETE, path, { req, params -> handler(req.copy(pathParams = params)) })
    }
    
    fun patch(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.PATCH, path, { req, params -> handler(req.copy(pathParams = params)) })
    }
    
    private fun registerRoute(
        method: HttpMethod,
        path: String, 
        handler: (HttpRequest, Map<String, String>) -> HttpResponse
    ) {
        val pathMatcher = PathMatcher(path)
        routes.add(RouteEntry(method, pathMatcher, handler))
    }

    fun resolve(request: HttpRequest): HttpResponse {
        // Find the first matching route
        val matchingRoute = routes.find { route ->
            route.method == request.method && route.pathMatcher.matches(request.path)
        }
        
        return if (matchingRoute != null) {
            // Extract path parameters and pass them to the handler
            val params = matchingRoute.pathMatcher.extractParams(request.path)
            matchingRoute.handler(request, params)
        } else {
            HttpResponse.notFound("404 Not Found - No route found for ${request.method} ${request.path}")
        }
    }
}