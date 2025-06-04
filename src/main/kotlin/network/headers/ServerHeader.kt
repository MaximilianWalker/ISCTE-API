package network.headers

class ServerHeader(value: String = "KotlinHttpServer") : HttpHeader<String>("Server", value) {
    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) {
        setValue(raw.trim())
    }

    override fun serialize(): String = value ?: ""
}