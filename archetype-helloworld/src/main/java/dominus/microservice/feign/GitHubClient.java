package dominus.microservice.feign;

import feign.Param;
import feign.RequestLine;

import java.util.List;

/**
 * Created: 7/19/17
 * Author: shawguo
 */
public interface GitHubClient {
    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);

    static class Contributor {
        String login;
        int contributions;
    }
}

