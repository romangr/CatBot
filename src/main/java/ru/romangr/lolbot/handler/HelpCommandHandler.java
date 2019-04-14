package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import ru.romangr.lolbot.handler.action.TelegramAction;
import ru.romangr.lolbot.handler.action.TelegramActionFactory;
import ru.romangr.lolbot.telegram.model.Chat;

import java.util.List;

@StaticCommand(BotCommand.HELP)
@RequiredArgsConstructor
public class HelpCommandHandler extends StaticCommandHandler {

    private static final String HELP_STRING = "Type /cat to get a random cat :3";

    private final TelegramActionFactory actionFactory;

    @Override
    protected List<TelegramAction> handleCommand(Chat chat, String messageText) {
        return List.of(actionFactory.newSendMessageAction(chat, HELP_STRING));
    }
}
