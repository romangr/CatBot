import Model.Bot;
import Model.Chat;
import Model.Message;
import Model.MessageToSend;
import Model.Update;
import Model.User;

import java.util.List;

/**
 * Roman 27.10.2016.
 */
public interface LolBot extends Bot {
    Bot getMe();
    List<Update> getUpdates();
    Message sendMessage(MessageToSend messageToSend);

    void start();
    void processUpdates();
}
