package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;

@RequiredArgsConstructor
public class UnknownCommandHandler {

    private final TelegramActionExecutor actionExecutor;

    public Exceptional<HandlingResult> handle(Chat chat) {
        this.actionExecutor.sendMessage(chat, "Incorrect command syntax");
        return Exceptional.exceptional(HandlingResult.HANDLED);
    }

}
