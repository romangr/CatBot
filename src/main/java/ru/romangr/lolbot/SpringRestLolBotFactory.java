package ru.romangr.lolbot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Roman 23.09.2017.
 */
public class SpringRestLolBotFactory {

    public static RestBot newBot(Path propertiesFile) {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(propertiesFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SpringRestLolBot(properties);
    }

    private SpringRestLolBotFactory() {

    }
}
