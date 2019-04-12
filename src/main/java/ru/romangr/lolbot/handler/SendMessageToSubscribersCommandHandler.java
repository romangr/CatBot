package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.subscription.SubscribersService;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;

import java.util.Optional;

@Slf4j
@StaticCommand(BotCommand.SEND_MESSAGE_TO_SUBSCRIBERS)
@RequiredArgsConstructor
public class SendMessageToSubscribersCommandHandler extends StaticCommandHandler {

    private final TelegramActionExecutor actionExecutor;
    private final SubscribersService subscribersService;
    private final Optional<Long> adminChatId;

    @Override
    protected void handleCommand(Chat chat, String messageText) {
        if (isMessageFromAdmin(chat)) {
            subscribersService.sendMessageToSubscribers();
        } else {
            this.actionExecutor.sendMessage(chat, "No permission to execute");
        }
    }

    private Boolean isMessageFromAdmin(Chat chat) {
        return adminChatId.map(adminId -> adminId.equals(Long.valueOf(chat.getId()))).orElse(false);
    }

}
