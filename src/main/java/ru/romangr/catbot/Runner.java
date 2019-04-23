package ru.romangr.catbot;

import static ru.romangr.catbot.SpringRestCatBotFactory.newBot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.exceptional.Exceptional;

/**
 * Roman 01.04.2017.
 */
@Slf4j
public class Runner {

  private static final String DEFAULT_SETTING_FILE = "settings.data";
  private static final String ENABLE_ENV_SETTINGS_ENV_VAR = "CATBOT_ENV_SETTINGS";
  private static final String GIT_INFO_PROPERTY = "GIT_REVISION";
  private static final String BUILD_INFO_PROPERTY_FILE = "build.properties";

  public static void main(String[] args) {
    Locale.setDefault(Locale.US);
    String gitRevision = Exceptional
        .getExceptional(() -> getProperties(Paths.get(BUILD_INFO_PROPERTY_FILE)))
        .map(map -> map.get(GIT_INFO_PROPERTY))
        .ifException(e -> log.error("Error reading build info", e))
        .getOrDefault("[UNKNOWN]");
    log.info("Starting bot. Build info: {}", gitRevision);
    Exceptional<RestBot> bot;
    if (args.length > 0) {
      bot = newBot(getProperties(Paths.get(args[0])));
    } else if (Files.exists(Paths.get(DEFAULT_SETTING_FILE))) {
      bot = newBot(getProperties(Paths.get("settings.data")));
    } else if (System.getenv().containsKey(ENABLE_ENV_SETTINGS_ENV_VAR)) {
      bot = newBot(System.getenv());
    } else {
      log.error("Please provide settings file as an argument");
      return;
    }
    bot.ifValue(RestBot::start).ifException(e -> log.error("Bot initialization error", e));
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private static Map<String, String> getProperties(Path propertiesFile) {
    Properties properties = new Properties();
    properties.load(Files.newInputStream(propertiesFile));
    return (Hashtable) properties;
  }
}
