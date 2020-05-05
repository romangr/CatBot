package ru.romangr.catbot.subscription

class MessageToSubscribers {
    val text: String?
    val documentId: String?
    val type: MessageToSubscribersType

    private constructor(text: String? = null, documentId: String? = null,
                        type: MessageToSubscribersType) {
        this.text = text
        this.documentId = documentId
        this.type = type
    }

    companion object Factory {
        fun textMessage(text: String): MessageToSubscribers {
            return MessageToSubscribers(text, null, MessageToSubscribersType.TEXT)
        }

        fun documentMessage(videoId: String): MessageToSubscribers {
            return MessageToSubscribers(null, videoId, MessageToSubscribersType.DOCUMENT)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageToSubscribers

        if (text != other.text) return false
        if (documentId != other.documentId) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text?.hashCode() ?: 0
        result = 31 * result + (documentId?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageToSubscribers(text=$text, documentId=$documentId, type=$type)"
    }


}

enum class MessageToSubscribersType {
    TEXT,
    DOCUMENT
}
