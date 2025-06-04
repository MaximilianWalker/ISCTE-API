package network.headers

enum class ConnectionValue {
    KEEP_ALIVE,
    CLOSE;

    override fun toString(): String = name.lowercase()
}

class ConnectionHeader(value: ConnectionValue = ConnectionValue.CLOSE) : HttpHeader<ConnectionValue>("Connection", value) {
    override fun validate(): Boolean = true

    override fun parse(raw: String) {
        val newValue = try {
            ConnectionValue.valueOf(raw.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid Connection value: $raw. Allowed values are: ${ConnectionValue.entries.joinToString()}")
        }
        setValue(newValue)
    }

    override fun serialize(): String = value.toString().replace("_", "-")
}
