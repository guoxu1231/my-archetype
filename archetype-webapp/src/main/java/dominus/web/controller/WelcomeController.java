package dominus.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.Map;

@Controller
public class WelcomeController {

    @GetMapping("/welcome")
    public String welcome(Map<String, Object> model) {
        model.put("time", new Date().toString());
        model.put("name", "shawguo");
        return "t2";
    }

    @RequestMapping("/foo")
    public String foo(Map<String, Object> model) {
        throw new RuntimeException("Foo");
    }

}