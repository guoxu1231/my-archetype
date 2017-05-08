package dominus.web.sparkjava.support;


import org.springframework.stereotype.Component;

@Component
public class DummyService {

    public String echo(String str) {
        return "Hello , I'm dummy service - " + str;
    }

}
