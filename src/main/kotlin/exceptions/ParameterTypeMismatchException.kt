package core.exceptions

/**
 * Exception thrown when a parameter type does not match the expected request type.
 * This occurs when path parameters, query parameters, or body parameters cannot be
 * converted to the expected type in controller method parameters.
 */
class ParameterTypeMismatchException(
    val parameterName: String,
    val expectedType: String,
    val actualValue: String,
    message: String = "Parameter '$parameterName' expected type '$expectedType' but received value '$actualValue'"
) : Exception(message)
