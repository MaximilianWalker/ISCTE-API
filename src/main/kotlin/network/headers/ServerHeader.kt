package network.headers

class ServerHeader(value: String = "KotlinHttpServer") : HttpHeader<String>("Server", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = false
        var isMandatoryOnResponse: Boolean = true
        init { register("Server", ServerHeader::class) }
    }

    override fun validate(): Boolean = value?.isNotBlank() ?: false

    override fun parse(raw: String) {
        setValue(raw.trim())
    }

    override fun serialize(): String = value ?: ""
}