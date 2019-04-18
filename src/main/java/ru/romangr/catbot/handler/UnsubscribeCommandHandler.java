package ru.romangr.catbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.handler.action.TelegramAction;
import ru.romangr.catbot.handler.action.TelegramActionFactory;
import ru.romangr.catbot.subscription.SubscribersService;
import ru.romangr.catbot.telegram.model.Chat;

import java.util.List;

@Slf4j
@StaticCommand(BotCommand.UNSUBSCRIBE)
@RequiredArgsConstructor
public class UnsubscribeCommandHandler extends StaticCommandHandler {

    private static final String MESSAGE_TO_UNSUBSCRIBED_ONE = "You have unsubscribed!";
    private static final String YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE = "You are not a subscriber yet!";

    private final TelegramActionFactory actionFactory;
    private final SubscribersService subscribersService;

    @Override
    protected List<TelegramAction> handleCommand(Chat chat, String messageText) {
        boolean isDeleted = subscribersService.deleteSubscriber(chat);
        String message;
        if (isDeleted) {
            message = MESSAGE_TO_UNSUBSCRIBED_ONE;
            log.info("Someone have unsubscribed. Total subscribers: {}.", subscribersService.getSubscribersCount());
        } else {
            message = YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE;
        }
        return List.of(actionFactory.newSendMessageAction(chat, message));
    }

}
