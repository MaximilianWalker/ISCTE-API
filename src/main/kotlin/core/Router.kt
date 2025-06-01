package core

import network.HttpRequest
import network.HttpResponse

class Router {
    private val routes = mutableMapOf<String, (HttpRequest) -> HttpResponse>()

    fun get(path: String, handler: (HttpRequest) -> HttpResponse) {
        routes["GET:$path"] = handler
    }

    fun post(path: String, handler: (HttpRequest) -> HttpResponse) {
        routes["POST:$path"] = handler
    }

    fun resolve(request: HttpRequest): HttpResponse {
        return routes["${request.method}:${request.path}"]?.invoke(request)
            ?: HttpResponse(404, mapOf("Content-Type" to "text/plain"), "404 Not Found")
    }
}