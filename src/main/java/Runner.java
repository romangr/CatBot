import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Logger;

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

    private static LolBot bot; //= new SpringRestLolBot(Paths.get("src\\test\\resources\\settings.data"));

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length > 0) {
            bot = new SpringRestLolBot(Paths.get(args[0]));
        } else {
            LOGGER.warning("Please provide settings file as an argument");
            return;
        }
        bot.start();
    }
}
