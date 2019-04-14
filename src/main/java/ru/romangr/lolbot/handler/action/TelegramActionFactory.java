package ru.romangr.lolbot.handler.action;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import ru.romangr.lolbot.telegram.model.Chat;

@RequiredArgsConstructor
public class TelegramActionFactory {

    private final RestTemplate restTemplate;
    private final String requestUrl;

    public TelegramAction newSendMessageAction(Chat chat, String text) {
        return new SendMessageAction(restTemplate, requestUrl, chat, text);
    }

}
