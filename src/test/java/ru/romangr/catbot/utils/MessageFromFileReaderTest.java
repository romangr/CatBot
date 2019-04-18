package ru.romangr.catbot.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roman 17.07.2017.
 */
class MessageFromFileReaderTest {

    private static final Path MESSAGE_FILE_TEST = Paths.get("src\\test\\resources\\message_to_subscribers.txt");
    private static final String MESSAGE = "Message to subscribers";
    private MessageFromFileReader reader;

    @BeforeEach
    void setUp() throws Exception {
        try (OutputStream outputStream = Files.newOutputStream(MESSAGE_FILE_TEST, StandardOpenOption.CREATE_NEW)) {
            outputStream.write(MESSAGE.getBytes());
        }
        reader = new MessageFromFileReader(MESSAGE_FILE_TEST);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(MESSAGE_FILE_TEST);
    }

    @Test
    void readMessageAndDeleteFile() {
        String message = reader.read();
        assertThat(message).isEqualTo(MESSAGE);
    }
}
