package ru.romangr.catbot.telegram;

import ru.romangr.catbot.telegram.model.Bot;
import ru.romangr.catbot.telegram.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.romangr.catbot.telegram.dto.GetMeResponse;
import ru.romangr.catbot.telegram.dto.GetUpdatesResponse;
import ru.romangr.catbot.utils.URLBuilder;
import ru.romangr.exceptional.Exceptional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TelegramRequestExecutor {

    private static final String GET_ME_METHOD = "getMe";
    private static final String GET_UPDATES_METHOD = "getUpdates";
    private static final String OFFSET_PARAMETER = "offset";

    private final RestTemplate restTemplate;
    private final String requestUrl;

    public Bot getMe() {
        return restTemplate.getForObject(new URLBuilder().withHost(requestUrl).withPath(GET_ME_METHOD).build(),
                GetMeResponse.class).getResult();
    }

    public Exceptional<List<Update>> getUpdates(long offset) {
        return Exceptional.getExceptional(() -> restTemplate.getForObject(
                new URLBuilder()
                        .withHost(requestUrl)
                        .withPath(GET_UPDATES_METHOD)
                        .withParameter(OFFSET_PARAMETER, offset)
                        .build(),
                GetUpdatesResponse.class)
        ).safelyMap(response -> {
            if (!response.isOk()) {
                throw new RuntimeException("Error during getting updates");
            }
            return response.getUpdates();
        });
    }

    public boolean isConnectedToInternet() {
        try {
            restTemplate.getForObject(new URLBuilder().withHost(requestUrl).withPath(GET_ME_METHOD).build(),
                    GetMeResponse.class);
            log.debug("Connected to the Internet");
            return true;
        } catch (ResourceAccessException e) {
            log.warn("No connection", e);
            return false;
        } catch (Exception e) {
            log.error("Exception during connectivity check", e);
            return false;
        }
    }
}
