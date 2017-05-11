import CatFinder.CatFinder;
import JsonEntities.GetMeResponse;
import JsonEntities.GetUpdatesResponse;
import Model.Bot;
import Model.Chat;
import Model.Message;
import Model.MessageToSend;
import Model.Update;
import Model.User;
import Utils.URLBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;

/**
 * Roman 27.10.2016.
 */
public class SpringRestLolBot implements LolBot {

    private static final String REQUEST_URL_PROPERTY_NAME = "REQUEST_URL";
    private static final String CAT_API_KEY_PROPERTY_NAME = "CAT_API_KEY";
    private static final String GET_UPDATES_METHOD = "getUpdates";
    private static final String GET_ME_METHOD = "getMe";
    private static final String OFFSET_PARAMETER = "offset";
    private String REQUEST_URL;
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
        REQUEST_URL = Optional.ofNullable(properties.getProperty(REQUEST_URL_PROPERTY_NAME))
                .orElseThrow(RuntimeException::new);
        me = updateMe();
        Optional.ofNullable(properties.getProperty(CAT_API_KEY_PROPERTY_NAME))
                .ifPresent(CatFinder::setCatApiKey);
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
            int maxUpdateId = updates.stream().mapToInt(Update::getId)
                    .max().getAsInt();
            currentUpdateOffset = maxUpdateId + 1;
        }
        return updates;
    }

    @Override
    public Message sendMessage(MessageToSend messageToSend) {
        String url = new URLBuilder().withHost(REQUEST_URL)
                .withPath("sendMessage")
                .build();
        return restTemplate.postForObject(url, messageToSend, Message.class);
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
