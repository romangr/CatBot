import Model.Bot;
import Model.Update;

import java.util.List;

/**
 * Roman 27.10.2016.
 */
public interface LolBot extends Bot {
    Bot getMe();
    List<Update> getUpdates();
}
