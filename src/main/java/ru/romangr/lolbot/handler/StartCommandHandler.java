package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.MessageToSend;

@StaticCommand(BotCommand.START)
@RequiredArgsConstructor
public class StartCommandHandler extends StaticCommandHandler {

    private static final String HELP_STRING = "Type /cat to get a random cat :3";

    private final TelegramActionExecutor actionExecutor;

    @Override
    protected void handleCommand(Chat chat, String messageText) {
        this.actionExecutor.sendMessage(new MessageToSend(chat, HELP_STRING));
    }
}
