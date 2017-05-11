import CatFinder.CatFinder;
import Model.Chat;
import Model.MessageToSend;
import Model.Update;
import Subscription.SubscribersRepository;
import Utils.DelayCalculator;

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

    private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());

    private static final String HELP_STRING = "Type /cat to get a random cat :3";
    private static LolBot bot; //= new SpringRestLolBot(Paths.get("src\\test\\resources\\settings.data"));
    private static Runnable updatesProcessor = () -> {
        try {
            Runner.processUpdates(bot);
        } catch (Exception e) {
            LOGGER.warning(e.getClass() + " " + e.getMessage());
        }
    };
    private static Runnable sendCatsToSubscribers = new Runnable() {
        @Override
        public void run() {
            String catUrl = CatFinder.getCat().getUrl();
            for (Chat chat : subscribersRepository.getAllSubscribers()) {
                bot.sendMessage(new MessageToSend(chat, catUrl));
            }
        }
    };
    private static final SubscribersRepository subscribersRepository =
            new SubscribersRepository(Paths.get("subscribers.data"));

    public static void main(String[] args) {
        if (args.length > 0) {
            bot = new SpringRestLolBot(Paths.get(args[0]));
        }
        Locale.setDefault(Locale.US);
        LOGGER.info(String.format("Bot started! Total subscribers: %d", subscribersRepository.getSubscribersCount()));
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(updatesProcessor, 0, 30, TimeUnit.SECONDS);

        Duration delay = DelayCalculator.calculateDelayToRunAtParticularTime(18);
        scheduledExecutorService.scheduleAtFixedRate(sendCatsToSubscribers, delay.getSeconds(),
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
        for (Update update : updates) {
            Chat chat = getChatFromUpdate(update);
            switch (update.getMessage().getText()) {
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
                    if (update.getMessage().getChat() != null) {
                        boolean isAdded = subscribersRepository.addSubscriber(update.getMessage().getChat());
                        String message;
                        if (isAdded) {
                            message = "You are subscriber!";
                        } else {
                            message = "You are already subscribed!";
                        }
                        bot.sendMessage(new MessageToSend(chat, message));
                    }
                    LOGGER.info(String.format("New subscriber. Total subscribers: %d.",
                            subscribersRepository.getSubscribersCount()));
                    commandsReceived = true;
                    break;
                case "/unsubscribe":
                    if (update.getMessage().getChat() != null) {
                        boolean isDeleted = subscribersRepository.deleteSubscriber(update.getMessage().getChat());
                        String message;
                        if (isDeleted) {
                            message = "You have unsubscribed!";
                        } else {
                            message = "You are not a subscriber!";
                        }
                        bot.sendMessage(new MessageToSend(chat, message));
                    }
                    LOGGER.info(String.format("Someone have unsubscribed. Total subscribers: %d.",
                            subscribersRepository.getSubscribersCount()));
                    commandsReceived = true;
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
    }

    private static Chat getChatFromUpdate(Update update) {
        return update.getMessage().getChat();
    }
}
