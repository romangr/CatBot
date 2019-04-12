package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.subscription.SubscribersService;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.MessageToSend;

@Slf4j
@StaticCommand(BotCommand.UNSUBSCRIBE)
@RequiredArgsConstructor
public class UnsubscribeCommandHandler extends StaticCommandHandler {

    private static final String MESSAGE_TO_UNSUBSCRIBED_ONE = "You have unsubscribed!";
    private static final String YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE = "You are not a subscriber yet!";

    private final TelegramActionExecutor actionExecutor;
    private final SubscribersService subscribersService;

    @Override
    protected void handleCommand(Chat chat, String messageText) {
        boolean isDeleted = subscribersService.deleteSubscriber(chat);
        String message;
        if (isDeleted) {
            message = MESSAGE_TO_UNSUBSCRIBED_ONE;
        } else {
            message = YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE;
        }
        this.actionExecutor.sendMessage(new MessageToSend(chat, message));
        log.info("Someone have unsubscribed. Total subscribers: {}.", subscribersService.getSubscribersCount());
    }

}
