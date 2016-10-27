import JsonEntities.GetMeResponse;
import Model.Bot;
import Model.Update;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * Roman 27.10.2016.
 */
public class SpringRestLolBotTest {

    private SpringRestLolBot lolBot = new SpringRestLolBot(Paths.get("src\\test\\resources\\settings.data"));

    @Test
    public void getMe() throws Exception {
        Bot me = lolBot.getMe();
        Assert.assertThat(me.getFirstName(), is("GRI10"));
        Assert.assertThat(me.getId(), is(102395457));
        Assert.assertThat(me.getUsername(), is("GritenLolBot"));
    }

    @Test
    public void getUpdates() throws Exception {
        List<Update> updates = lolBot.getUpdates();
        List<Update> updates1 = lolBot.getUpdates();
        Assert.assertThat(updates1.isEmpty(), is(true));
    }
}