package ru.romangr.catbot.catfinder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.catbot.catfinder.Model.Cat;
import ru.romangr.catbot.utils.URLBuilder;

import java.net.URI;
import java.util.List;

/**
 * Roman 01.04.2017.
 */
@Slf4j
public class CatFinder {

    private final RestTemplate restTemplate;
    private final RequestEntity<Void> requestEntity;

    public CatFinder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        URI url = URI.create(prepareUrlWithoutApiKey().build());
        requestEntity = getResponseEntity(url);
    }

    public CatFinder(RestTemplate restTemplate, String apiKey) {
        this.restTemplate = restTemplate;
        URI url = URI.create(prepareUrlWithoutApiKey()
                .withParameter("api_key", apiKey)
                .build());
        requestEntity = getResponseEntity(url);
    }

    public Exceptional<Cat> getCat() {
        return Exceptional
                .getExceptional(() -> restTemplate.exchange(requestEntity,
                        new ParameterizedTypeReference<List<Cat>>(){}))
                .map(HttpEntity::getBody)
                .map(list -> list.get(0));
    }


    private URLBuilder prepareUrlWithoutApiKey() {
        return new URLBuilder()
                .withHost("http://api.thecatapi.com")
                .withPath("api/images/get")
                .withParameter("format", "json");
    }

    private RequestEntity<Void> getResponseEntity(URI url) {
        return RequestEntity.get(url)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .build();
    }
}
