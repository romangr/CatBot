package ru.romangr.catbot.utils;

import static java.util.Optional.ofNullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PropertiesResolver {

  private static final String CAT_API_KEY_PROPERTY_NAME = "CAT_API_KEY";
  private static final String TELEGRAM_API_URL_PROPERTY_NAME = "TELEGRAM_API_URL";
  private static final String BOT_TOKEN_PROPERTY_NAME = "BOT_TOKEN";
  private static final String BOT_NAME_PROPERTY_NAME = "BOT_NAME";
  private static final int DEFAULT_UPDATES_CHECK_PERIOD = 30;
  private static final String SUBSCRIBERS_FILE_PATH_PROPERTY_NAME = "SUBSCRIBERS_FILE_PATH";
  private static final String DEFAULT_SUBSCRIBERS_FILE_PATH = "data/subscribers.json";
  private static final String SEND_TO_SUBSCRIBERS_TIME_PROPERTY_NAME
      = "TIME_OCLOCK_TO_SEND_MESSAGE_TO_SUBSCRIBERS";
  private static final int DEFAULT_TIME_TO_SEND_MESSAGE_TO_SUBSCRIBERS = 17;
  private static final String DB_FILE_PATH_PROPERTY_NAME = "DB_FILE_PATH";
  private static final String DEFAULT_DB_FILE_PATH = "data/cat_bot.sqlite";

  private final Map<String, String> properties;
  private final String buildInfo;

  public String getRequestUrl() {
    return ofNullable(properties.get(TELEGRAM_API_URL_PROPERTY_NAME)).flatMap(
            apiUrl -> ofNullable(properties.get(BOT_TOKEN_PROPERTY_NAME)).map(token -> apiUrl + token))
        .orElseThrow(() -> new RuntimeException("Request url can't be resolved!"));
  }

  public int getTimeToSendMessageToSubscribers() {
    return ofNullable(properties.get(SEND_TO_SUBSCRIBERS_TIME_PROPERTY_NAME))
        .map(Integer::valueOf)
        .orElse(DEFAULT_TIME_TO_SEND_MESSAGE_TO_SUBSCRIBERS);
  }

  public int getUpdatesCheckPeriod() {
    return ofNullable(properties.get("UPDATES_CHECK_PERIOD")).map(Integer::valueOf)
        .orElse(DEFAULT_UPDATES_CHECK_PERIOD);
  }

  public Optional<Long> getAdminChatId() {
    return ofNullable(properties.get("ADMIN_CHAT_ID")).map(Long::valueOf);
  }

  public String getCatApiKey() {
    return ofNullable(properties.get(CAT_API_KEY_PROPERTY_NAME))
        .orElseThrow(() -> new RuntimeException("No Cat API key provided"));
  }

  public String getBotName() {
    return ofNullable(properties.get(BOT_NAME_PROPERTY_NAME))
        .orElseThrow(() -> new RuntimeException("No bot name provided"));
  }

  public String getDbFilePath() {
    return ofNullable(properties.get(DB_FILE_PATH_PROPERTY_NAME))
        .orElse(DEFAULT_DB_FILE_PATH);
  }

  public Path getSubscribersFilePath() {
    return ofNullable(properties.get(SUBSCRIBERS_FILE_PATH_PROPERTY_NAME))
        .or(() -> Optional.of(DEFAULT_SUBSCRIBERS_FILE_PATH))
        .map(Paths::get)
        .get();
  }

  public String getBuildInfo() {
    return this.buildInfo;
  }
}
