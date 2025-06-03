package core

/**
 * Class that handles matching request paths against route templates with parameters
 */
class PathMatcher(private val routePath: String) {
    private val paramNames = mutableListOf<String>()
    private val regex: Regex
    
    init {
        // Convert path template like "/users/{id}/profile" to regex
        val patternString = routePath
            .replace(Regex("\\{([^/]+)\\}")) { matchResult ->
                val paramName = matchResult.groupValues[1]
                paramNames.add(paramName)
                "([^/]+)"
            }
            // Escape special regex characters except for the ones we just added
            .replace(Regex("([.+*?\\[\\]()^\\$])")) { "\\\${it.groupValues[1]}" }
        
        regex = Regex("^$patternString$")
    }
    
    /**
     * Check if the given path matches this route template
     */
    fun matches(path: String): Boolean {
        return regex.matches(path)
    }
    
    /**
     * Extract parameter values from the path
     * @return Map of parameter name to value
     */
    fun extractParams(path: String): Map<String, String> {
        val matches = regex.find(path) ?: return emptyMap()
        
        return paramNames.mapIndexed { index, name ->
            name to (matches.groupValues[index + 1])
        }.toMap()
    }
}