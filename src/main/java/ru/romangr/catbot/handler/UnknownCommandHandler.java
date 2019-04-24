package ru.romangr.catbot.handler;

import lombok.RequiredArgsConstructor;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.model.Chat;

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
