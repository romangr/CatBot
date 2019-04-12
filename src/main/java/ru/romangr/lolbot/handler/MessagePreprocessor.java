package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessagePreprocessor {

    private final String botName;

    String process(String messageText) {
        return messageText.replace(botName, "").trim();
    }

}
