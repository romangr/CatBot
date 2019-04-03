package ru.romangr.lolbot;

import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.MessageToSend;
import ru.romangr.lolbot.telegram.model.Update;
import ru.romangr.lolbot.catfinder.CatFinder;
import ru.romangr.lolbot.subscription.SubscribersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class UpdatesHandler {

    private static final String HELP_STRING = "Type /cat to get a random cat :3";
    private static final String MESSAGE_TO_NEW_SUBSCRIBER = "Thank you for subscription!";
    private static final String YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE = "You have already subscribed!";
    private static final String MESSAGE_TO_UNSUBSCRIBED_ONE = "You have unsubscribed!";
    private static final String YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE = "You are not a subscriber yet!";
    private static final Path MESSAGE_TO_SUBSCRIBERS_FILE = Paths.get("message_to_subscribers.txt");

    private final TelegramActionExecutor commandExecutor;
    private final CatFinder catFinder;
    private final Optional<Long> adminChatId;
    private final SubscribersService subscribersService;


    public boolean handleUpdate(StringBuilder logString, boolean commandsReceived, Update update) {
        Chat chat = getChatFromUpdate(update);
        String messageText = update.getMessage().getText();
        messageText = messageText.replace("@GritenLolBot", "").trim();
        switch (messageText) {
            case "/start":
                this.commandExecutor.sendMessage(new MessageToSend(chat, HELP_STRING));
                commandsReceived = true;
                break;
            case "/cat":
                log.info("/cat");
                this.commandExecutor.sendMessage(chat, catFinder.getCat().getUrl());
                commandsReceived = true;
                break;
            case "/help":
                this.commandExecutor.sendMessage(chat, HELP_STRING);
                commandsReceived = true;
                break;
            case "/subscribe":
                commandsReceived = subscribeUser(update, chat);
                break;
            case "/unsubscribe":
                commandsReceived = unsubscribeUser(update, chat);
                break;
            case "/smts":
                commandsReceived = true;
                if (isMessageFromAdmin(update)) {
                    subscribersService.sendMessageToSubscribers();
                } else {
                    this.commandExecutor.sendMessage(chat, "No permission to execute");
                }
                break;
            default: {
                if (messageText.startsWith("/amts")) {
                    String response;
                    if (isMessageFromAdmin(update)) {
                        String[] splitMessage = messageText.split(" ", 2);
                        subscribersService.addMessageToSubscribers(splitMessage[1]);
                        response = String.format("Message added to queue. Its number is %d",
                                subscribersService.getMessageQueueLength());
                        this.commandExecutor.sendMessage(chat, response);
                        log.info(response);
                    } else {
                        response = "Incorrect command syntax";
                        this.commandExecutor.sendMessage(chat, response);
                        log.info(response);
                    }
                }
            }
        }
        Stream.of(chat.getTitle(), chat.getUsername(), chat.getFirstName(), chat.getLastName())
                .filter(Objects::nonNull).findFirst().ifPresent(identifier -> {
            logString.append(identifier);
            logString.append(" ");
        });
        return commandsReceived;
    }

    private boolean unsubscribeUser(Update update, Chat chat) {
        if (update.getMessage().getChat() != null) {
            boolean isDeleted = subscribersService.deleteSubscriber(update.getMessage().getChat());
            String message;
            if (isDeleted) {
                message = MESSAGE_TO_UNSUBSCRIBED_ONE;
            } else {
                message = YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE;
            }
            this.commandExecutor.sendMessage(new MessageToSend(chat, message));
        }
        log.info("Someone have unsubscribed. Total subscribers: {}.",
                subscribersService.getSubscribersCount());
        return true;
    }

    private boolean subscribeUser(Update update, Chat chat) {
        if (update.getMessage().getChat() != null) {
            boolean isAdded = subscribersService.addSubscriber(update.getMessage().getChat());
            String message;
            if (isAdded) {
                message = MESSAGE_TO_NEW_SUBSCRIBER;
            } else {
                message = YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE;
            }
            this.commandExecutor.sendMessage(new MessageToSend(chat, message));
        }
        log.info("New subscriber. Total subscribers: {}.", subscribersService.getSubscribersCount());
        return true;
    }

    private Boolean isMessageFromAdmin(Update update) {
        return adminChatId.map(adminId -> adminId == getChatFromUpdate(update).getId()).orElse(false);
    }

    private static Chat getChatFromUpdate(Update update) {
        return update.getMessage().getChat();
    }
}
