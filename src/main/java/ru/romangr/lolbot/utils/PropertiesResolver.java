package ru.romangr.lolbot.utils;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class PropertiesResolver {

    private static final String CAT_API_KEY_PROPERTY_NAME = "CAT_API_KEY";
    private static final String TELEGRAM_API_URL_PROPERTY_NAME = "TELEGRAM_API_URL";
    private static final String BOT_TOKEN_PROPERTY_NAME = "BOT_TOKEN";
    private static final String BOT_NAME_PROPERTY_NAME = "BOT_NAME";
    private static final int DEFAULT_UPDATES_CHECK_PERIOD = 30;

    private final Map<String, String> properties;

    public String getRequestUrl() {
        return ofNullable(properties.get(TELEGRAM_API_URL_PROPERTY_NAME)).flatMap(
                apiUrl -> ofNullable(properties.get(BOT_TOKEN_PROPERTY_NAME)).map(token -> apiUrl + token))
                .orElseThrow(() -> new RuntimeException("Request url can't be resolved!"));
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
}
