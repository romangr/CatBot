package ru.romangr.catbot.handler.action;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import ru.romangr.catbot.telegram.model.Chat;

@RequiredArgsConstructor
public class TelegramActionFactory {

  private final RestTemplate restTemplate;
  private final String requestUrl;

  public TelegramAction newSendMessageAction(Chat chat, String text) {
    return new SendMessageAction(restTemplate, requestUrl, text, chat);
  }

}
