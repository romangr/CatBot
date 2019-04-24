package ru.romangr.catbot.handler.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.romangr.catbot.telegram.model.ExecutionResult;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.catbot.telegram.dto.SendMessageResponse;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.MessageToSend;
import ru.romangr.catbot.utils.URLBuilder;

@RequiredArgsConstructor
class SendMessageAction implements TelegramAction {

  private final RestTemplate restTemplate;
  private final String requestUrl;
  private final String text;
  @Getter private final Chat chat;

  @Override
  public Exceptional<ExecutionResult> execute() {
    return sendMessageSafely(chat, text);
  }

  private Exceptional<ExecutionResult> sendMessageSafely(Chat chat, String message) {
    return Exceptional.getExceptional(() -> {
      String url = new URLBuilder().withHost(requestUrl).withPath("sendMessage").build();
      return restTemplate
          .postForObject(url, new MessageToSend(chat, message), SendMessageResponse.class);
    })
        .map(SendMessageResponse::getResult)
        .map(message1 -> ExecutionResult.SUCCESS)
        .resumeOnException(e -> {
              if (e instanceof HttpClientErrorException && isTooManyRequestStatus(e)) {
                return ExecutionResult.RATE_LIMIT_FAILURE;
              }
              return ExecutionResult.FAILURE;
            }
        );
  }

  private boolean isTooManyRequestStatus(Exception e) {
    return ((HttpClientErrorException) e).getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS);
  }

}
