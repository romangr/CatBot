package ru.romangr.lolbot.handler.action;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.telegram.dto.SendMessageResponse;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.Message;
import ru.romangr.lolbot.telegram.model.MessageToSend;
import ru.romangr.lolbot.utils.URLBuilder;

@RequiredArgsConstructor
class SendMessageAction implements TelegramAction {

    private final RestTemplate restTemplate;
    private final String requestUrl;
    private final Chat chat;
    private final String text;

    @Override
    public Exceptional<?> execute() {
        return sendMessageSafely(chat, text);
    }

    private Exceptional<Message> sendMessageSafely(Chat chat, String message) {
        return Exceptional.getExceptional(() -> {
            String url = new URLBuilder().withHost(requestUrl).withPath("sendMessage").build();
            return restTemplate.postForObject(url, new MessageToSend(chat, message), SendMessageResponse.class);
        }).map(SendMessageResponse::getResult);
    }

}
