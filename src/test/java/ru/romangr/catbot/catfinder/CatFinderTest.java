package ru.romangr.catbot.catfinder;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.romangr.exceptional.Exceptional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Roman 01.04.2017.
 */
class CatFinderTest {

    private RestTemplate restTemplate = new RestTemplate();
    private CatFinder catFinder = new CatFinder(restTemplate);

    @Test
    void getCatSuccessfully() {
        MockRestServiceServer.createServer(restTemplate)
                .expect(requestTo("https://api.thecatapi.com/api/images/get?format=json"))
                .andRespond(withSuccess("[{\"url\": \"https://example.com/test.jpg\"}]",
                        MediaType.APPLICATION_JSON_UTF8));

        Exceptional<Cat> cat = catFinder.getCat();

        assertThat(cat.isValuePresent()).isTrue();
        assertThat(cat.getValue().getUrl()).isEqualTo("https://example.com/test.jpg");
    }
}
