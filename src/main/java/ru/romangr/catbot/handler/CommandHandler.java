package ru.romangr.catbot.handler;

import ru.romangr.exceptional.Exceptional;
import ru.romangr.catbot.handler.action.TelegramAction;
import ru.romangr.catbot.telegram.model.Chat;

import java.util.List;

public abstract class CommandHandler {

    public Exceptional<HandlingResult> handle(Chat chat, String messageText) {
        if (isApplicable(messageText)) {
            return Exceptional.getExceptional(() -> HandlingResult.builder()
                    .actions(handleCommand(chat, messageText))
                    .status(HandlingStatus.HANDLED)
                    .build());
        }
        return Exceptional.exceptional(HandlingResult.builder()
                .status(HandlingStatus.SKIPPED)
                .build());
    }

    abstract boolean isApplicable(String messageText);

    protected abstract List<TelegramAction> handleCommand(Chat chat, String messageText);
}
