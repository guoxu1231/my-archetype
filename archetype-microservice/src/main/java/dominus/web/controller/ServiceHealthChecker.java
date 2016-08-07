package dominus.web.controller;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.Assert.assertTrue;

@RestController
public class ServiceHealthChecker {

    protected final Logger logger = LoggerFactory.getLogger(ServiceHealthChecker.class);
    @Autowired
    private ApplicationContext context;

    @RequestMapping("/health")
    @ResponseBody
    String healthCheck() {
        logger.info("prepare to start service health check...");
        assertTrue(context != null);
        try {
            //EE: groovy web console
            HttpResponse<String> jsonResponse = Unirest.post("http://localhost:8090/rest/script")
                    .header("accept", "application/json")
                    .field("script", "9*9")
                    .asString();

            System.out.println(jsonResponse.getBody());
            assertTrue(jsonResponse.getBody().contains("\"result\":\"81\""));

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return "OK";


    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
