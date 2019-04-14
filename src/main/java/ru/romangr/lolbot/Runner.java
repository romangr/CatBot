package ru.romangr.lolbot;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.exceptional.Exceptional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static ru.romangr.lolbot.SpringRestLolBotFactory.newBot;

/**
 * Roman 01.04.2017.
 */
@Slf4j
public class Runner {
    // todo: write good tests for bot

    private static final String DEFAULT_SETTING_FILE = "settings.data";
    private static final String ENABLE_ENV_SETTINGS_ENV_VAR = "CATBOT_ENV_SETTINGS";

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
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
