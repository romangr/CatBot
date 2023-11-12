package ru.romangr.catbot;

import static ru.romangr.catbot.SpringRestCatBotFactory.newBot;

import java.io.InputStream;
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
    String buildInfo = Exceptional
        .exceptional(Runner.class.getClassLoader().getResourceAsStream(BUILD_INFO_PROPERTY_FILE))
        .safelyMap(Runner::getPropertiesFromStream)
        .map(properties -> properties.getProperty(GIT_INFO_PROPERTY))
        .ifException(e -> log.error("Error reading build info", e))
        .getOrDefault("[UNKNOWN]");
    log.info("Starting bot. Build info: {}", buildInfo);
    Exceptional<RestBot> bot;
    if (args.length > 0) {
      bot = newBot(getPropertiesFromFile(Paths.get(args[0])), buildInfo);
    } else if (Files.exists(Paths.get(DEFAULT_SETTING_FILE))) {
      bot = newBot(getPropertiesFromFile(Paths.get("settings.data")), buildInfo);
    } else if (System.getenv().containsKey(ENABLE_ENV_SETTINGS_ENV_VAR)) {
      bot = newBot(System.getenv(), buildInfo);
    } else {
      log.error("Please provide settings file as an argument");
      return;
    }
    bot.ifValue(RestBot::start)
        .ifException(e -> log.error("Bot initialization error", e))
        .getOrThrow();
  }

  @SneakyThrows
  private static Properties getPropertiesFromStream(InputStream stream) {
    Properties properties = new Properties();
    properties.load(stream);
    return properties;
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private static Map<String, String> getPropertiesFromFile(Path propertiesFile) {
    return (Hashtable) getPropertiesFromStream(Files.newInputStream(propertiesFile));
  }
}
