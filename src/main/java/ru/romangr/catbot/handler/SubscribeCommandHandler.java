package ru.romangr.catbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.subscription.SubscribersService;
import ru.romangr.catbot.telegram.model.Chat;

import java.util.List;

@Slf4j
@StaticCommand(BotCommand.SUBSCRIBE)
@RequiredArgsConstructor
public class SubscribeCommandHandler extends StaticCommandHandler {

    private static final String MESSAGE_TO_NEW_SUBSCRIBER = "Thank you for subscription!";
    private static final String YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE = "You have already subscribed!";

    private final TelegramActionFactory actionFactory;
    private final SubscribersService subscribersService;

    @Override
    protected List<TelegramAction> handleCommand(Chat chat, String messageText) {
        boolean isAdded = subscribersService.addSubscriber(chat);
        String message;
        if (isAdded) {
            message = MESSAGE_TO_NEW_SUBSCRIBER;
            log.info("New subscriber. Total subscribers: {}.", subscribersService.getSubscribersCount());
        } else {
            message = YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE;
        }
        return List.of(actionFactory.newSendMessageAction(chat, message));
    }

}
