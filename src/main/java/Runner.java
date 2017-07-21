import CatFinder.CatFinder;
import Model.Chat;
import Model.MessageToSend;
import Model.Update;
import Subscription.SubscribersRepository;
import Utils.DelayCalculator;
import Utils.MessageFromFileReader;
import org.springframework.web.client.ResourceAccessException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Roman 01.04.2017.
 */
public class Runner {
    // todo: make SpringRestLolBot abstract with abstract method processUpdates(), interface SubscribableLolBot with
    // method processSubscribers or smth like that. Thread pool should be inside SpringRestLolBot and should start by
    // method start() in LolBot interface. Main thread should process console input by another class
    // ConsoleCommandProcessor.
    // Architecture like this will allow get updates processing period and subscribers processing time from
    // property file

    // todo: write good tests for bot

    private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());

    private static final String HELP_STRING = "Type /cat to get a random cat :3";
    private static final String MESSAGE_TO_NEW_SUBSCRIBER = "Thank you for subscription!";
    private static final String YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE = "You have already subscribed!";
    private static final String MESSAGE_TO_UNSUBSCRIBED_ONE = "You have unsubscribed!";
    private static final String YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE = "You are not a subscriber yet!";
    private static final Path MESSAGE_TO_SUBSCRIBERS_FILE = Paths.get("message_to_subscribers.txt");
    private static final int TIME_OCLOCK_TO_SEND_MESSAGE_TO_SUBSCRIBERS = 20;

    private static LolBot bot; //= new SpringRestLolBot(Paths.get("src\\test\\resources\\settings.data"));
    private static final SubscribersRepository subscribersRepository =
            new SubscribersRepository(Paths.get("subscribers.data"));
    private static Runnable updatesProcessor = () -> {
        try {
            Runner.processUpdates(bot);
        } catch (Exception e) {
            LOGGER.warning(e.getClass() + " " + e.getMessage());
        }
    };
    private static Runnable sendMessageToSubscribers = new Runnable() {
        @Override
        public void run() {
            String message = null;
            boolean areGreetingsNeeded = true;
            if (Files.exists(MESSAGE_TO_SUBSCRIBERS_FILE)) {
                try {
                    message = new MessageFromFileReader(MESSAGE_TO_SUBSCRIBERS_FILE, true).read();
                    areGreetingsNeeded = false;
                } catch (RuntimeException e) {
                    LOGGER.warning("Exception during reading message to subscribers from file");
                }
            }
            if (message == null) {
                message = CatFinder.getCat().getUrl();
            }

            for (Chat chat : subscribersRepository.getAllSubscribers()) {
                if (areGreetingsNeeded) {
                    final StringBuilder messageToSubscriber = new StringBuilder().append("Your daily cat");
                    Stream.of(chat.getTitle(), chat.getFirstName(), chat.getLastName(), chat.getUsername())
                            .filter(Objects::nonNull)
                            .findFirst()
                            .ifPresent(identifier -> {
                                        messageToSubscriber.append(", ")
                                                .append(identifier)
                                                .append("!");
                                    }
                            );
                    messageToSubscriber.append("\n")
                            .append(message);
                    bot.sendMessage(new MessageToSend(chat, messageToSubscriber.toString()));
                } else {
                    bot.sendMessage(new MessageToSend(chat, message));
                }
            }
            LOGGER.info(String.format("Cat has been sent to %d subscribers",
                    subscribersRepository.getSubscribersCount()));
        }
    };

    public static void main(String[] args) {
        if (args.length > 0) {
            bot = new SpringRestLolBot(Paths.get(args[0]));
        } else {
            LOGGER.warning("Please provide settings file as an argument");
            return;
        }
        Locale.setDefault(Locale.US);
        LOGGER.info(String.format("Bot started! Total subscribers: %d", subscribersRepository.getSubscribersCount()));
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(updatesProcessor, 0, 30, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(() -> LOGGER.info("All systems are fine!"),
                1, 1, TimeUnit.HOURS);

        Duration delay =
                DelayCalculator.calculateDelayToRunAtParticularTime(TIME_OCLOCK_TO_SEND_MESSAGE_TO_SUBSCRIBERS);
        LOGGER.info(String.format("Next sending to subscribers in %d minutes", delay.getSeconds() / 60));
        scheduledExecutorService.scheduleAtFixedRate(sendMessageToSubscribers, delay.getSeconds(),
                DelayCalculator.getSecondsFromHours(24), TimeUnit.SECONDS);
    }

    private static void processUpdates(LolBot bot) {
        List<Update> updates = bot.getUpdates();
        if (updates.isEmpty()) {
            return;
        }
        StringBuilder logString = new StringBuilder();
        logString.append(new Date())
                .append(": ")
                .append(updates.size())
                .append(" updates received from: ");
        boolean commandsReceived = false;
        try {
            for (Update update : updates) {
                Chat chat = getChatFromUpdate(update);
                String messageText = update.getMessage().getText();
                messageText = messageText.replace("@GritenLolBot", "");
                switch (messageText) {
                    case "/start":
                        bot.sendMessage(new MessageToSend(chat, HELP_STRING));
                        commandsReceived = true;
                        break;
                    case "/cat":
                        bot.sendMessage(new MessageToSend(chat, CatFinder.getCat().getUrl()));
                        commandsReceived = true;
                        break;
                    case "/help":
                        bot.sendMessage(new MessageToSend(chat, HELP_STRING));
                        commandsReceived = true;
                        break;
                    case "/subscribe":
                        commandsReceived = subscribeUser(bot, update, chat);
                        break;
                    case "/unsubscribe":
                        commandsReceived = unsubscribeUser(bot, update, chat);
                        break;
                    default:
                        continue;
                }
                Stream.of(chat.getTitle(), chat.getUsername(), chat.getFirstName(), chat.getLastName())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .ifPresent(identifier -> {
                                    logString.append(identifier);
                                    logString.append(" ");
                                }
                        );
            }
            if (commandsReceived) {
                LOGGER.info(logString.toString());
            }
        } catch (NullPointerException | ResourceAccessException e) {
            LOGGER.warning(e.getClass().getSimpleName());
        } catch (Exception e) {
            LOGGER.warning(e.getClass() + " " + e.getMessage() + " Exception during processing :" + updates);
        }
    }

    private static boolean unsubscribeUser(LolBot bot, Update update, Chat chat) {
        if (update.getMessage().getChat() != null) {
            boolean isDeleted = subscribersRepository.deleteSubscriber(update.getMessage().getChat());
            String message;
            if (isDeleted) {
                message = MESSAGE_TO_UNSUBSCRIBED_ONE;
            } else {
                message = YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE;
            }
            bot.sendMessage(new MessageToSend(chat, message));
        }
        LOGGER.info(String.format("Someone have unsubscribed. Total subscribers: %d.",
                subscribersRepository.getSubscribersCount()));
        return true;
    }

    private static boolean subscribeUser(LolBot bot, Update update, Chat chat) {
        if (update.getMessage().getChat() != null) {
            boolean isAdded = subscribersRepository.addSubscriber(update.getMessage().getChat());
            String message;
            if (isAdded) {
                message = MESSAGE_TO_NEW_SUBSCRIBER;
            } else {
                message = YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE;
            }
            bot.sendMessage(new MessageToSend(chat, message));
        }
        LOGGER.info(String.format("New subscriber. Total subscribers: %d.",
                subscribersRepository.getSubscribersCount()));
        return true;
    }

    private static Chat getChatFromUpdate(Update update) {
        return update.getMessage().getChat();
    }
}
