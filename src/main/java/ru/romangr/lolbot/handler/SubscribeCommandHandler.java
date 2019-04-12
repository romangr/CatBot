package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.subscription.SubscribersService;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.MessageToSend;

@Slf4j
@StaticCommand(BotCommand.SUBSCRIBE)
@RequiredArgsConstructor
public class SubscribeCommandHandler extends StaticCommandHandler {

    private static final String MESSAGE_TO_NEW_SUBSCRIBER = "Thank you for subscription!";
    private static final String YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE = "You have already subscribed!";

    private final TelegramActionExecutor actionExecutor;
    private final SubscribersService subscribersService;

    @Override
    protected void handleCommand(Chat chat, String messageText) {
        boolean isAdded = subscribersService.addSubscriber(chat);
        String message;
        if (isAdded) {
            message = MESSAGE_TO_NEW_SUBSCRIBER;
        } else {
            message = YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE;
        }
        this.actionExecutor.sendMessage(new MessageToSend(chat, message));
        log.info("New subscriber. Total subscribers: {}.", subscribersService.getSubscribersCount());
    }

}
