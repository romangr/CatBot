package ru.romangr.catbot.catfinder;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.romangr.exceptional.Exceptional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Roman 01.04.2017.
 */
class CatFinderTest {

  private RestTemplate restTemplate = new RestTemplate();
  private CatFinder catFinder = new CatFinder(restTemplate);

  @Test
  void getCatSuccessfully() {
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    server.expect(requestTo("https://api.thecatapi.com/api/images/get?format=json"))
        .andRespond(withSuccess("[{\"url\": \"https://example.com/test.jpg\"}]",
            MediaType.APPLICATION_JSON_UTF8));
    server.expect(requestTo("https://example.com/test.jpg"))
        .andRespond(withSuccess().contentType(MediaType.IMAGE_JPEG));

    Exceptional<Cat> cat = catFinder.getCat();

    assertThat(cat.isValuePresent()).isTrue();
    assertThat(cat.getValue().getUrl()).isEqualTo("https://example.com/test.jpg");
  }

  @Test
  void getCatSuccessfullyAfterRetryWithUnexpectedContentType() {
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    server.expect(requestTo("https://api.thecatapi.com/api/images/get?format=json"))
        .andRespond(withSuccess("[{\"url\": \"https://example.com/test.jpg\"}]",
            MediaType.APPLICATION_JSON_UTF8));
    server.expect(requestTo("https://example.com/test.jpg"))
        .andRespond(withSuccess().contentType(MediaType.APPLICATION_XML));
    server.expect(requestTo("https://api.thecatapi.com/api/images/get?format=json"))
        .andRespond(withSuccess("[{\"url\": \"https://example.com/test2.jpg\"}]",
            MediaType.APPLICATION_JSON_UTF8));
    server.expect(requestTo("https://example.com/test2.jpg"))
        .andRespond(withSuccess().contentType(MediaType.IMAGE_JPEG));

    Exceptional<Cat> cat = catFinder.getCat();

    assertThat(cat.isValuePresent()).isTrue();
    assertThat(cat.getValue().getUrl()).isEqualTo("https://example.com/test2.jpg");
  }

  @Test
  void getCatSuccessfullyAfterRetryWithUnexpectedStatusCode() {
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    server.expect(requestTo("https://api.thecatapi.com/api/images/get?format=json"))
        .andRespond(withSuccess("[{\"url\": \"https://example.com/test.jpg\"}]",
            MediaType.APPLICATION_JSON_UTF8));
    server.expect(requestTo("https://example.com/test.jpg"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));
    server.expect(requestTo("https://api.thecatapi.com/api/images/get?format=json"))
        .andRespond(withSuccess("[{\"url\": \"https://example.com/test2.jpg\"}]",
            MediaType.APPLICATION_JSON_UTF8));
    server.expect(requestTo("https://example.com/test2.jpg"))
        .andRespond(withSuccess().contentType(MediaType.IMAGE_JPEG));

    Exceptional<Cat> cat = catFinder.getCat();

    assertThat(cat.isValuePresent()).isTrue();
    assertThat(cat.getValue().getUrl()).isEqualTo("https://example.com/test2.jpg");
  }

  @Test
  void getCatSuccessfullyWithRedirect() {
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    server.expect(requestTo("https://api.thecatapi.com/api/images/get?format=json"))
        .andRespond(withSuccess("[{\"url\": \"https://example.com/test.jpg\"}]",
            MediaType.APPLICATION_JSON_UTF8));
    server.expect(requestTo("https://example.com/test.jpg"))
        .andRespond(withStatus(HttpStatus.MOVED_PERMANENTLY).location(
            URI.create("https://example.com/test2.jpg")).contentType(MediaType.TEXT_HTML));
    server.expect(requestTo("https://example.com/test2.jpg"))
        .andRespond(withSuccess().contentType(MediaType.IMAGE_JPEG));

    Exceptional<Cat> cat = catFinder.getCat();

    assertThat(cat.isValuePresent()).isTrue();
    assertThat(cat.getValue().getUrl()).isEqualTo("https://example.com/test.jpg");
  }
}
