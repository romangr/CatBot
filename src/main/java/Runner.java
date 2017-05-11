import CatFinder.CatFinder;
import Model.Chat;
import Model.MessageToSend;
import Model.Update;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Roman 01.04.2017.
 */
public class Runner {

    private static final String HELP_STRING = "Type /cat to get a random cat :3";
    private static LolBot bot; //= new SpringRestLolBot(Paths.get("src\\test\\resources\\settings.data"));
    private static Runnable updatesProcessor = () -> {
        try {
            Runner.processUpdates(bot);
        } catch (Exception e) {
            System.out.println(e.getClass() + " " + e.getMessage());
        }
    };

    public static void main(String[] args) {
        if (args.length > 0) {
            bot = new SpringRestLolBot(Paths.get(args[0]));
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(updatesProcessor, 0, 30, TimeUnit.SECONDS);
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
            System.out.println(logString.toString());
        }
    }

    private static Chat getChatFromUpdate(Update update) {
        return update.getMessage().getChat();
    }
}
