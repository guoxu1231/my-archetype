package dominus.web.test;

import dominus.web.RestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(RestApplication.class)
@WebIntegrationTest("server.port:9000")
public class SampleJerseyApplicationTests {

    private RestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void contextLoads() {
        ResponseEntity<String> entity = this.restTemplate
                .getForEntity("http://localhost:9000/rest/echo?name=shawguo", String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("Hello shawguo!", entity.getBody());
    }

//    @Test
//    public void reverse() {
//        ResponseEntity<String> entity = this.restTemplate.getForEntity(
//                "http://localhost:" + this.port + "/reverse?input=olleh", String.class);
//        assertEquals(HttpStatus.OK, entity.getStatusCode());
//        assertEquals("hello", entity.getBody());
//    }
//
//    @Test
//    public void validation() {
//        ResponseEntity<String> entity = this.restTemplate
//                .getForEntity("http://localhost:" + this.port + "/reverse", String.class);
//        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
//    }

}