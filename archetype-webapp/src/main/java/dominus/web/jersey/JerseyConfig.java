package dominus.web.jersey;

import dominus.web.jersey.endpoint.EchoEndpoint;
import dominus.web.jersey.endpoint.GroovyScriptEndpoint;
import dominus.web.jersey.endpoint.HelloWorldEndpoint;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;


@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(HelloWorldEndpoint.class);
        register(EchoEndpoint.class);
        register(GroovyScriptEndpoint.class);
    }

}
