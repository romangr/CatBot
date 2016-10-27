import JsonEntities.GetMeResponse;
import JsonEntities.GetUpdatesResponse;
import Model.Bot;
import Model.Update;
import Utils.URLBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Roman 27.10.2016.
 */
public class SpringRestLolBot implements LolBot {

    private String REQUEST_URL;
    private static final String GET_UPDATES_METHOD = "getUpdates";
    private static final String GET_ME_METHOD = "getMe";
    private static final String OFFSET_PARAMETER = "offset";
    private Bot me;

    private RestTemplate restTemplate = new RestTemplate();
    private int currentUpdateOffset = 0;

    public SpringRestLolBot(String requestURL) {
        REQUEST_URL = requestURL;
        me = updateMe();
    }

    public SpringRestLolBot(Path propertyFile) {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(propertyFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        REQUEST_URL = Optional.ofNullable(properties.getProperty("REQUEST_URL"))
                            .orElseThrow(RuntimeException::new);
        me = updateMe();
    }

    public Bot getMe() {
        return me;
    }

    private Bot updateMe() {
        return restTemplate.getForObject(
                new URLBuilder()
                        .withHost(REQUEST_URL)
                        .withPath(GET_ME_METHOD)
                        .build(),
                GetMeResponse.class
        ).getResult();
    }

    @Override
    public List<Update> getUpdates() {
        GetUpdatesResponse updatesResponse = restTemplate.getForObject(
                new URLBuilder()
                        .withHost(REQUEST_URL)
                        .withPath(GET_UPDATES_METHOD)
                        .withParameter(OFFSET_PARAMETER, currentUpdateOffset)
                        .build(),
                GetUpdatesResponse.class
                );
        if (!updatesResponse.isOk())
            throw new RuntimeException("Cant't get updates!");
        List<Update> updates = updatesResponse.getUpdates();
        if (!updates.isEmpty()) {
            currentUpdateOffset = updates.get(updates.size() - 1).getId() + 1;
        }
        return updates;
    }

    @Override
    public int getId() {
        return me.getId();
    }

    @Override
    public String getFirstName() {
        return me.getFirstName();
    }

    @Override
    public String getUsername() {
        return me.getUsername();
    }
}
