package dominus.microservice.feign;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.slf4j.Slf4jLogger;
import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;
import dominus.microservice.feign.GitHubClient.Contributor;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created: 7/19/17
 * Author: shawguo
 */
public class TestFeignClient extends DominusJUnit4TestBase {

    @Test
    public void testBasic() {
        GitHubClient gitHubClient = Feign.builder()
                .decoder(new GsonDecoder())
                //SLF4JModule allows directing Feign's logging to SLF4J, allowing you to easily use a logging backend of your choice (Logback, Log4J, etc.)
                //<logger name="feign" level="DEBUG"/> also required.
                .logLevel(feign.Logger.Level.FULL).logger(new Slf4jLogger())
                .target(GitHubClient.class, "https://api.github.com");

        // Fetch and print a list of the contributors to this library.
        List<Contributor> contributors = gitHubClient.contributors("OpenFeign", "feign");
        for (Contributor contributor : contributors) {
            System.out.println(contributor.login + " (" + contributor.contributions + ")");
        }
        assertTrue(contributors.size() > 0);
    }


}
