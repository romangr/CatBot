package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.catfinder.CatFinder;
import ru.romangr.lolbot.catfinder.Model.Cat;
import ru.romangr.lolbot.handler.action.TelegramAction;
import ru.romangr.lolbot.handler.action.TelegramActionFactory;
import ru.romangr.lolbot.telegram.model.Chat;

import java.util.List;

@Slf4j
@StaticCommand(BotCommand.CAT)
@RequiredArgsConstructor
public class CatCommandHandler extends StaticCommandHandler {

    private static final String ERROR_MESSAGE
            = "Sorry, your cat can't be delivered now, please try later";
    private final TelegramActionFactory actionFactory;
    private final CatFinder catFinder;

    @Override
    protected List<TelegramAction> handleCommand(Chat chat, String messageText) {
        return catFinder.getCat()
                .map(Cat::getUrl)
                .ifException(e -> log.warn("/cat can't be handled", e))
                .resumeOnException(e -> ERROR_MESSAGE)
                .map(message -> List.of(actionFactory.newSendMessageAction(chat, message)))
                .getValue();
    }
}
