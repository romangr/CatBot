import ru.romangr.lolbot.SpringRestLolBotFactory;
import ru.romangr.lolbot.RestBot;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Roman 01.04.2017.
 */
public class Runner {
  // todo: write good tests for bot

  private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());

  public static void main(String[] args) {
    Locale.setDefault(Locale.US);
    RestBot bot;
    if (args.length > 0) {
      bot = SpringRestLolBotFactory.newBot(Paths.get(args[0]));
    } else if (Files.exists(Paths.get("settings.data"))) {
      bot = SpringRestLolBotFactory.newBot(Paths.get("settings.data"));
    } else {
      LOGGER.severe("Please provide settings file as an argument");
      return;
    }
    bot.start();
  }
}
