package ru.romangr.lolbot.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.catfinder.CatFinder;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.TelegramRequestExecutor;
import ru.romangr.lolbot.telegram.model.Chat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class SubscribersService {

    private static final Path MESSAGE_TO_SUBSCRIBERS_FILE = Paths.get("message_to_subscribers.txt");

    private final SubscribersRepository subscribersRepository;
    private final TelegramRequestExecutor requestExecutor;
    private final TelegramActionExecutor actionExecutor;
    private final Queue<String> messagesToSubscribers = new LinkedList<>();
    private final CatFinder catFinder;

    public boolean sendMessageToSubscribers() {
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
            log.warn("Can't send result to subscribers now, will send it later");
            return true;
        }

        if (!messagesToSubscribers.isEmpty()) {
            message = messagesToSubscribers.poll();
            log.info("Message from queue will be sent. There are {} posts left in the queue",
                    messagesToSubscribers.size());
            areGreetingsNeeded = false;
        }

        if (message == null) {
            message = catFinder.getCat().getUrl();
        }

        for (Chat chat : subscribersRepository.getAllSubscribers()) {
            if (areGreetingsNeeded) {
                final StringBuilder messageToSubscriber = new StringBuilder().append("Your daily cat");
                Stream.of(chat.getTitle(), chat.getFirstName(), chat.getLastName(), chat.getUsername())
                        .filter(Objects::nonNull).findFirst().ifPresent(identifier -> {
                    messageToSubscriber.append(", ").append(identifier).append("!");
                });
                messageToSubscriber.append("\n").append(message);
                actionExecutor.sendMessageSafely(chat, messageToSubscriber.toString())
                        .ifException(e -> log.warn("sending result to " + chat, e));

            } else {
                actionExecutor.sendMessageSafely(chat, message)
                        .ifException(e -> log.warn("sending result to " + chat, e));
            }
        }
        log.info(String.format("Cat has been sent to %d subscribers", subscribersRepository.getSubscribersCount()));
        return false;
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
