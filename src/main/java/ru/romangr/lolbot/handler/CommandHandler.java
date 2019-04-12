package ru.romangr.lolbot.handler;

import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.telegram.model.Chat;

public abstract class CommandHandler {

    public Exceptional<HandlingResult> handle(Chat chat, String messageText) {
        if (isApplicable(messageText)) {
            return Exceptional.getExceptional(() -> {
                handleCommand(chat, messageText);
                return HandlingResult.HANDLED;
            });
        }
        return Exceptional.exceptional(HandlingResult.SKIPPED);
    }

    abstract boolean isApplicable(String messageText);

    protected abstract void handleCommand(Chat chat, String messageText);
}
