package ru.romangr.lolbot.telegram;

import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.Message;
import ru.romangr.lolbot.telegram.model.MessageToSend;
import ru.romangr.lolbot.telegram.dto.SendMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import ru.romangr.lolbot.utils.URLBuilder;
import ru.romangr.exceptional.Exceptional;

@RequiredArgsConstructor
public class TelegramActionExecutor {

    private final RestTemplate restTemplate;
    private final String requestUrl;

    public SendMessageResponse sendMessage(MessageToSend messageToSend) {
        String url = new URLBuilder().withHost(requestUrl).withPath("sendMessage").build();
        return restTemplate.postForObject(url, messageToSend, SendMessageResponse.class);
    }

    public Exceptional<Message> sendMessageSafely(Chat chat, String message) {
        return Exceptional.getExceptional(() -> this.sendMessage(chat, message));
    }

    public Message sendMessage(Chat chat, String text) {
        return this.sendMessage(new MessageToSend(chat, text)).getResult();
    }

}
