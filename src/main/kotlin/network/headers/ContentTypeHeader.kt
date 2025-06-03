package network.headers

import network.ContentType

class ContentTypeHeader(value: ContentType = ContentType.TEXT) : HttpHeader<ContentType>("Content-Type", value) {

    companion object {
        var isMandatoryOnRequest: Boolean = false
        var isMandatoryOnResponse: Boolean = true
        init { register("Content-Type", ContentTypeHeader::class) }
    }

    override fun validate(): Boolean = value.let { it?.let { it1 -> ContentType.fromHeader(it1.value) } != null }

    override fun parse(raw: String) {
        ContentType.fromHeader(raw)?.let { setValue(it) }
    }

    override fun serialize(): String = value?.value ?: ""
}
