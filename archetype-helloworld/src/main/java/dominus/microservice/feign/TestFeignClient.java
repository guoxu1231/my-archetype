package dominus.microservice.feign;

import dominus.microservice.feign.GitHubClient.Contributor;
import feign.Feign;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.ribbon.RibbonClient;
import feign.slf4j.Slf4jLogger;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;

import java.util.List;

import static com.netflix.config.ConfigurationManager.getConfigInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created: 7/19/17
 * Author: shawguo
 */
public class TestFeignClient extends DominusJUnit4TestBase {

    String clientName;

    @Rule
    public final MockWebServer server1 = new MockWebServer();

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

    /**
     * Round Robin Loadbalacing Strategy;
     * LoadBalancerCommand;
     * In ribbon, ConnectException and SocketTimeoutException are RetryableException.
     */
    @Test
    public void testRibbonClientLoadBalancer() {

        clientName = "github-client";
        // The Sun HTTP Client retries all requests once on an IOException, which makes testing retry code harder than would
        // be ideal. We can only disable it for post, so lets at least do that.
        System.setProperty("sun.net.http.retryPost", "false");
        server1.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
        server1.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
        server1.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        getConfigInstance().setProperty(clientName + ".ribbon.listOfServers", "api.github.com:443" + "," + "http://localhost:" + server1.getPort());
        getConfigInstance().setProperty(clientName + ".ribbon.MaxAutoRetries", 2);
        getConfigInstance().setProperty(clientName + ".ribbon.MaxAutoRetriesNextServer", 1);
        getConfigInstance().setProperty(clientName + ".ribbon.ReadTimeout", 2000);
        getConfigInstance().setProperty(clientName + ".ribbon.ConnectTimeout", 2000);

        GitHubClient gitHubClient = Feign.builder()
                .decoder(new GsonDecoder())
                .logLevel(feign.Logger.Level.FULL).logger(new Slf4jLogger())
                .retryer(Retryer.NEVER_RETRY)
                .client(RibbonClient.create()).target(GitHubClient.class, "https://" + clientName);

        List<Contributor> contributors = gitHubClient.contributors("OpenFeign", "feign");
        for (Contributor contributor : contributors) {
            System.out.println(contributor.login + " (" + contributor.contributions + ")");
        }
        assertTrue(contributors.size() > 0);
        assertEquals(3, server1.getRequestCount());
    }

    @After
    public void clearServerList() {
        getConfigInstance().clearProperty(clientName + ".ribbon.listOfServers");
    }
}
