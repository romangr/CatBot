package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Roman 17.07.2017.
 */
public class MessageFromFileReader {

    private static Logger LOGGER = Logger.getLogger(MessageFromFileReader.class.getName());
    private final Path messageFileTest;
    private final boolean deleteAfterRead;

    public MessageFromFileReader(Path messageFileTest, boolean deleteAfterRead) {
        this.messageFileTest = messageFileTest;
        this.deleteAfterRead = deleteAfterRead;
    }

    public MessageFromFileReader(Path messageFileTest) {
        this(messageFileTest, false);
    }

    public String read() {
        try  {
            String message = new String(Files.readAllBytes(messageFileTest), Charset.forName("UTF-8"));
            if (deleteAfterRead) {
                Files.deleteIfExists(messageFileTest);
            }
            return message;
        } catch (IOException e) {
            LOGGER.warning(e.getClass() + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
