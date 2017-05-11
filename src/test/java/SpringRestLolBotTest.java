import JsonEntities.GetMeResponse;
import Model.Bot;
import Model.Chat;
import Model.Message;
import Model.MessageToSend;
import Model.Update;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * Roman 27.10.2016.
 */
public class SpringRestLolBotTest {

    public static final String TEXT = "text";
    public static final int CHAT_ID = 48354307;
    private static final Chat CHAT = new Chat(CHAT_ID);
    private SpringRestLolBot lolBot = new SpringRestLolBot(Paths.get("src\\test\\resources\\settings.data"));

    @Test
    public void getMe() throws Exception {
        Bot me = lolBot.getMe();
        Assert.assertThat(me.getFirstName(), is("GRI10"));
        Assert.assertThat(me.getId(), is(102395457));
        Assert.assertThat(me.getUsername(), is("GritenLolBot"));
    }

//    @Test
//    public void getUpdates() throws Exception {
//        List<Update> updates = lolBot.getUpdates();
//        System.out.println(updates);
//        List<Update> updates1 = lolBot.getUpdates();
//        Assert.assertThat(updates1.isEmpty(), is(true));
//    }

//    @Test
//    public void sendMessage() throws Exception {
//        MessageToSend messageToSend = new MessageToSend(CHAT, TEXT);
//        Message message = lolBot.sendMessage(messageToSend);
//        Assert.assertThat(message.getText(), is(TEXT));
//    }
}