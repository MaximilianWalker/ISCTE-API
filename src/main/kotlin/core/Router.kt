package core

import network.HttpMethod
import network.HttpRequest
import network.HttpResponse

class Router {
    private data class RouteEntry(
        val method: HttpMethod,
        val routePath: String,
        val handler: (HttpRequest) -> HttpResponse
    ) {
        fun matches(path: String): Boolean {
            val routeSegments = routePath.trim('/').split('/')
            val pathSegments = path.trim('/').split('/')
            if (pathSegments.size != routeSegments.size) return false

            return routeSegments.zip(pathSegments).all { (routeSeg, pathSeg) ->
                routeSeg.startsWith("{") && routeSeg.endsWith("}") || routeSeg == pathSeg
            }
        }
    }

    private val routes = mutableListOf<RouteEntry>()

    fun get(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.GET, path, { req -> handler(req) })
    }

    fun post(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.POST, path, { req -> handler(req) })
    }

    fun put(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.PUT, path, { req -> handler(req) })
    }

    fun delete(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.DELETE, path, { req -> handler(req) })
    }

    fun patch(path: String, handler: (HttpRequest) -> HttpResponse) {
        registerRoute(HttpMethod.PATCH, path, { req -> handler(req) })
    }

    private fun registerRoute(
        method: HttpMethod,
        path: String,
        handler: (HttpRequest) -> HttpResponse
    ) {
        routes.add(RouteEntry(method, path, handler))
    }

    fun resolve(request: HttpRequest): HttpResponse {
        val matchingRoute = routes.find { route ->
            route.method == request.method && route.matches(request.path)
        }
        return matchingRoute?.handler?.invoke(request)
            ?: HttpResponse.notFound("404 Not Found - No route found for ${request.method} ${request.path}")
    }
}