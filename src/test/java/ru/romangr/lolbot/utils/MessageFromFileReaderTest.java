package ru.romangr.lolbot.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Roman 17.07.2017.
 */
public class MessageFromFileReaderTest {

    private static final Path MESSAGE_FILE_TEST = Paths.get("src\\test\\resources\\message_to_subscribers.txt");
    private static final String MESSAGE = "Message to subscribers";
    private MessageFromFileReader reader;

    @Before
    public void setUp() throws Exception {
        try (OutputStream outputStream = Files.newOutputStream(MESSAGE_FILE_TEST, StandardOpenOption.CREATE_NEW)) {
            outputStream.write(MESSAGE.getBytes());
        }
        reader = new MessageFromFileReader(MESSAGE_FILE_TEST);
    }

    @Test
    public void readMessageAndDeleteFile() throws Exception {
        String message = reader.read();
        assertThat(message, is(MESSAGE));
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(MESSAGE_FILE_TEST);
    }
}