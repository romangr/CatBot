package ru.romangr.catbot.executor.action

import lombok.RequiredArgsConstructor
import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.telegram.model.Chat

@RequiredArgsConstructor
class TelegramActionFactory(private val restTemplate: RestTemplate,
                            private val requestUrl: String) {

    fun newSendMessageAction(chat: Chat, text: String): TelegramAction {
        return SendMessageAction(restTemplate, requestUrl, text, chat)
    }

}
