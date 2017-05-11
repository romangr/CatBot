package CatFinder;

import CatFinder.Model.Cat;
import Utils.URLBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Roman 01.04.2017.
 */
public class CatFinder {

    private static final Logger LOGGER = Logger.getLogger(CatFinder.class.getName());

    private static String URL = new URLBuilder()
            .withHost("http://thecatapi.com")
            .withPath("api/images/get")
            //.withParameter("format", "xml")
            .withParameter("format", "html")
            //.withParameter("results_per_page", 2)
            .build();
    private static final Pattern PIC_URL_PATTERN = Pattern.compile("src=\"([\\w\\d./_:]+)\"");
    private static RestTemplate restTemplate = new RestTemplate();

    public static void setCatApiKey(String apiKey) {
        URL = new URLBuilder()
                .withHost("http://thecatapi.com")
                .withPath("api/images/get")
                .withParameter("api_key", apiKey)
                .withParameter("format", "html")
                .build();
    }

    public static Cat getCat() {
        /*
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
         */
//        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(2);
//        messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
//        messageConverters.add(new StringHttpMessageConverter());
        //messageConverters.add(new MappingJackson2XmlHttpMessageConverter());

        //restTemplate.setMessageConverters(messageConverters);
//        Response response = restTemplate.getForObject(
//                URL,
//                Response.class
//        );
        try {
            HttpStatus statusCode;
            String pictureURL;
            do {
                String rawResponse = restTemplate.getForObject(URL, String.class);
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
            LOGGER.warning(e.getClass() + " " + e.getMessage());
            return CatFinder.getCat();
        }
    }

    private CatFinder() {

    }
}
