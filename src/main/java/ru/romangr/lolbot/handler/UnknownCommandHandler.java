package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.handler.action.TelegramAction;
import ru.romangr.lolbot.handler.action.TelegramActionFactory;
import ru.romangr.lolbot.telegram.model.Chat;

import java.util.List;

@RequiredArgsConstructor
public class UnknownCommandHandler {

    private final TelegramActionFactory actionFactory;

    public Exceptional<HandlingResult> handle(Chat chat) {
        TelegramAction action
                = actionFactory.newSendMessageAction(chat, "Incorrect command syntax");
        return Exceptional.exceptional(HandlingResult.builder()
                .status(HandlingStatus.HANDLED)
                .actions(List.of(action))
                .build());
    }

}
