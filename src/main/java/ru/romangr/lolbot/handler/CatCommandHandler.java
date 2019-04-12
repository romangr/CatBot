package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import ru.romangr.lolbot.catfinder.CatFinder;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;

@StaticCommand(BotCommand.CAT)
@RequiredArgsConstructor
public class CatCommandHandler extends StaticCommandHandler {

    private final TelegramActionExecutor actionExecutor;
    private final CatFinder catFinder;

    @Override
    protected void handleCommand(Chat chat, String messageText) {
        this.actionExecutor.sendMessage(chat, catFinder.getCat().getUrl());
    }
}
