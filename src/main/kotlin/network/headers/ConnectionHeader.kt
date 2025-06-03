package network.headers

enum class ConnectionValue {
    KEEP_ALIVE,
    CLOSE;

    override fun toString(): String = name.lowercase()
}

class ConnectionHeader(value: ConnectionValue = ConnectionValue.KEEP_ALIVE) : HttpHeader<ConnectionValue>("Connection", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = true
        var isMandatoryOnResponse: Boolean = true
        init { register("Connection", ConnectionHeader::class) }
    }

    override fun validate(): Boolean = true

    override fun parse(raw: String) {
        val newValue = try {
            ConnectionValue.valueOf(raw.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid Connection value: $raw. Allowed values are: ${ConnectionValue.entries.joinToString()}")
        }
        setValue(newValue)
    }

    override fun serialize(): String = value.toString() ?: ""
}
