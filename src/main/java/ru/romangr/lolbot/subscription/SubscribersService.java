package ru.romangr.lolbot.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.catfinder.CatFinder;
import ru.romangr.lolbot.catfinder.Model.Cat;
import ru.romangr.lolbot.handler.action.TelegramAction;
import ru.romangr.lolbot.handler.action.TelegramActionFactory;
import ru.romangr.lolbot.telegram.TelegramRequestExecutor;
import ru.romangr.lolbot.telegram.model.Chat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class SubscribersService {

    private static final Path MESSAGE_TO_SUBSCRIBERS_FILE = Paths.get("message_to_subscribers.txt");
    private static final String ISSUES_DURING_SENDING_MESSAGE = "Can't send result to subscribers now, will send it later";

    private final SubscribersRepository subscribersRepository;
    private final TelegramRequestExecutor requestExecutor;
    private final Queue<String> messagesToSubscribers = new LinkedList<>();
    private final CatFinder catFinder;
    private final TelegramActionFactory actionFactory;

    public Exceptional<List<TelegramAction>> sendMessageToSubscribers() {
        String message = null;
        boolean areGreetingsNeeded = true;
        // if (Files.exists(MESSAGE_TO_SUBSCRIBERS_FILE)) {
        // try {
        // result = new MessageFromFileReader(MESSAGE_TO_SUBSCRIBERS_FILE, true).read();
        // areGreetingsNeeded = false;
        // log.info("Message from file will be sent");
        // } catch (RuntimeException e) {
        // LOGGER.warning("Exception during reading result to subscribers from file");
        // }
        // }

        if (!requestExecutor.isConnectedToInternet()) {
            log.warn(ISSUES_DURING_SENDING_MESSAGE);
            return Exceptional.exceptional(
                    new RuntimeException(ISSUES_DURING_SENDING_MESSAGE)
            );
        }

        if (!messagesToSubscribers.isEmpty()) {
            message = messagesToSubscribers.poll();
            log.info("Message from queue will be sent. There are {} posts left in the queue",
                    messagesToSubscribers.size());
            areGreetingsNeeded = false;
        }

        if (message == null) {
            Exceptional<String> catUrl = catFinder.getCat()
                    .ifException(e -> log.warn("Exception getting cat", e))
                    .map(Cat::getUrl);
            if (!catUrl.isValuePresent()) {
                return catUrl.map(s -> List.of());
            }
            message = catUrl.getValue();
        }

        List<TelegramAction> actions = new ArrayList<>(subscribersRepository.getSubscribersCount());
        for (Chat chat : subscribersRepository.getAllSubscribers()) {
            if (areGreetingsNeeded) {
                final StringBuilder messageToSubscriber = new StringBuilder().append("Your daily cat");
                Stream.of(chat.getTitle(), chat.getFirstName(), chat.getLastName(), chat.getUsername())
                        .filter(Objects::nonNull).findFirst().ifPresent(identifier -> {
                    messageToSubscriber.append(", ").append(identifier).append("!");
                });
                messageToSubscriber.append("\n").append(message);
                TelegramAction action = actionFactory.newSendMessageAction(chat, messageToSubscriber.toString());
                actions.add(action);
            } else {
                TelegramAction action = actionFactory.newSendMessageAction(chat, message);
                actions.add(action);
            }
        }
        log.info(String.format("Cat has been sent to %d subscribers", subscribersRepository.getSubscribersCount()));
        return Exceptional.exceptional(actions);
    }

    public int getSubscribersCount() {
        return subscribersRepository.getSubscribersCount();
    }

    public void addMessageToSubscribers(String message) {
        messagesToSubscribers.add(message);
    }

    public int getMessageQueueLength() {
        return messagesToSubscribers.size();
    }

    public boolean deleteSubscriber(Chat subscriber) {
        return subscribersRepository.deleteSubscriber(subscriber);
    }

    public boolean addSubscriber(Chat subscriber) {
        return subscribersRepository.addSubscriber(subscriber);
    }
}
