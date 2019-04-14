package ru.romangr.lolbot;

import lombok.extern.slf4j.Slf4j;
import ru.romangr.exceptional.Exceptional;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Roman 01.04.2017.
 */
@Slf4j
public class Runner {
    // todo: write good tests for bot

    private static final String DEFAULT_SETTING_FILE = "settings.data";

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Exceptional<RestBot> bot;
        if (args.length > 0) {
            bot = SpringRestLolBotFactory.newBot(Paths.get(args[0]));
        } else if (Files.exists(Paths.get(DEFAULT_SETTING_FILE))) {
            bot = SpringRestLolBotFactory.newBot(Paths.get("settings.data"));
        } else {
            log.error("Please provide settings file as an argument");
            return;
        }
        bot.ifValue(RestBot::start).ifException(e -> log.error("Bot initialization error", e));
    }
}
