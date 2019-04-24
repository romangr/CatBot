package ru.romangr.catbot.handler;

import lombok.RequiredArgsConstructor;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.model.Chat;

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
