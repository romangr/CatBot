package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.handler.action.TelegramAction;
import ru.romangr.lolbot.handler.action.TelegramActionFactory;
import ru.romangr.lolbot.subscription.SubscribersService;
import ru.romangr.lolbot.telegram.model.Chat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@StaticCommand(BotCommand.SEND_MESSAGE_TO_SUBSCRIBERS)
@RequiredArgsConstructor
public class SendMessageToSubscribersCommandHandler extends StaticCommandHandler {

    private final TelegramActionFactory actionFactory;
    private final SubscribersService subscribersService;
    private final Optional<Long> adminChatId;

    @Override
    protected List<TelegramAction> handleCommand(Chat chat, String messageText) {
        if (isMessageFromAdmin(chat)) {
            var exceptional = subscribersService.sendMessageToSubscribers()
                    .ifException(e -> log.warn("Exception during sending message to subscribers", e));
            // TODO: use `getOrDefault(...)`
            return exceptional.isValuePresent() ? exceptional.getValue() : Collections.emptyList();
        } else {
            TelegramAction action
                    = actionFactory.newSendMessageAction(chat, "No permission to execute");
            return List.of(action);
        }
    }

    private Boolean isMessageFromAdmin(Chat chat) {
        return adminChatId.map(adminId -> adminId.equals(Long.valueOf(chat.getId()))).orElse(false);
    }

}
