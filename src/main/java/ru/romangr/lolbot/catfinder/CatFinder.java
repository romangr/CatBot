package ru.romangr.lolbot.catfinder;

import ru.romangr.lolbot.catfinder.Model.Cat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.romangr.lolbot.utils.URLBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Roman 01.04.2017.
 */
@Slf4j
public class CatFinder {

    private static final Pattern PIC_URL_PATTERN = Pattern.compile("src=\"([\\w\\d./_:]+)\"");

    private final RestTemplate restTemplate;
    private final String url;
    private final String apiKey;

    public CatFinder(RestTemplate restTemplate, String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.url = new URLBuilder()
                .withHost("http://thecatapi.com")
                .withPath("api/images/get")
                .withParameter("format", "html")
                .build();
    }

//    public void setCatApiKey(String apiKey) {
//        URL = new URLBuilder()
//                .withHost("http://thecatapi.com")
//                .withPath("api/images/get")
//                .withParameter("api_key", apiKey)
//                .withParameter("format", "html")
//                .build();
//    }

    public Cat getCat() {
        try {
            HttpStatus statusCode;
            String pictureURL;
            do {
                String rawResponse = restTemplate.getForObject(url, String.class);
                Matcher matcher = PIC_URL_PATTERN.matcher(rawResponse);
                boolean b = matcher.find();
                pictureURL = matcher.group(1);

                URI uri = new URI(pictureURL);
                RequestEntity<String> stringRequestEntity = new RequestEntity<String>(HttpMethod.GET, uri);
                ResponseEntity<String> exchange = restTemplate.exchange(stringRequestEntity, String.class);
                statusCode = exchange.getStatusCode();
            } while (!statusCode.is2xxSuccessful());
            if (pictureURL != null) {
                return new Cat(pictureURL);
            } else {
                throw new RuntimeException();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.warn("Exception getting cat", e);
            return this.getCat();
        }
    }

}
