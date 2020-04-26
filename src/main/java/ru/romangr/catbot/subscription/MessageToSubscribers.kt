package ru.romangr.catbot.subscription

class MessageToSubscribers {
    val text: String?
    val videoId: String?
    val type: MessageToSubscribersType

    private constructor(text: String? = null, videoId: String? = null,
                        type: MessageToSubscribersType) {
        this.text = text
        this.videoId = videoId
        this.type = type
    }

    companion object Factory {
        fun textMessage(text: String): MessageToSubscribers {
            return MessageToSubscribers(text, null, MessageToSubscribersType.TEXT)
        }

        fun videoMessage(videoId: String): MessageToSubscribers {
            return MessageToSubscribers(null, videoId, MessageToSubscribersType.VIDEO)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageToSubscribers

        if (text != other.text) return false
        if (videoId != other.videoId) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text?.hashCode() ?: 0
        result = 31 * result + (videoId?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageToSubscribers(text=$text, videoId=$videoId, type=$type)"
    }


}

enum class MessageToSubscribersType {
    TEXT,
    VIDEO
}
