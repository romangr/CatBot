import ru.romangr.lolbot.telegram.model.Bot;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.MessageToSend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import ru.romangr.lolbot.SpringRestLolBot;
import ru.romangr.lolbot.SpringRestLolBotFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import ru.romangr.lolbot.telegram.dto.SendMessageResponse;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Roman 27.10.2016.
 */
public class SpringRestLolBotTest {

  private static final String TEXT = "test message";
  private static final int CHAT_ID = 48354307;
  private static final Chat CHAT = new Chat(CHAT_ID);
  private static final Path PROPERTY_FILE = Paths.get("src/test/resources/settings.data");
  private static final Properties PROPERTIES = new Properties();
  private SpringRestLolBot lolBot =
      SpringRestLolBotFactory.newBot(PROPERTY_FILE);

  @BeforeClass
  public static void setUp() throws Exception {
    PROPERTIES.load(Files.newInputStream(PROPERTY_FILE));
  }

  @Test
  public void getMe() throws Exception {
    String requestUrl = PROPERTIES.getProperty("REQUEST_URL");
    String botId = substringBefore(
        substringAfter(requestUrl, "https://api.telegram.org/bot"), ":");

    Bot me = lolBot.getMe();

    assertThat(me.getId(), is(Integer.valueOf(botId)));
  }

  @Test
  public void sendMessage() throws Exception {
    MessageToSend messageToSend = new MessageToSend(CHAT, TEXT);

    SendMessageResponse message = lolBot.sendMessage(messageToSend);

    assertThat(message.isOk(), is(true));
    assertThat(message.getResult().getText(), is(TEXT));
  }
}