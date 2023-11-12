package ru.romangr.catbot.subscription

data class MessageToSubscribers(
    val type: MessageToSubscribersType,
    val text: String? = null,
    val documentId: String? = null,
    val videoId: String? = null,
    val photoId: String? = null
) {

    companion object Factory {
        fun textMessage(text: String): MessageToSubscribers {
            return MessageToSubscribers(MessageToSubscribersType.TEXT, text = text)
        }

        fun documentMessage(documentId: String): MessageToSubscribers {
            return MessageToSubscribers(MessageToSubscribersType.DOCUMENT, documentId = documentId)
        }

        fun videoMessage(videoId: String): MessageToSubscribers {
            return MessageToSubscribers(MessageToSubscribersType.VIDEO, videoId = videoId)
        }

        fun photoMessage(photoId: String, caption: String? = null): MessageToSubscribers {
            return MessageToSubscribers(MessageToSubscribersType.PHOTO, photoId = photoId, text = caption)
        }
    }

}

enum class MessageToSubscribersType {
    TEXT,
    DOCUMENT,
    VIDEO,
    PHOTO
}
