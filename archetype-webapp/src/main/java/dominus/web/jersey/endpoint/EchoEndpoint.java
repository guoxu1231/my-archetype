package dominus.web.jersey.endpoint;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.springframework.stereotype.Component;

@Component
@Path("/echo")
public class EchoEndpoint {

    @GET
    public String echo(@QueryParam("name") String name) {
        return String.format("Hello %s!", name);
    }
}