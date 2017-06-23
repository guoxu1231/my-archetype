package dominus.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
public class HttpBasicController {
    @GetMapping("/basic")
    String home(HttpServletRequest request) {
        //EE: Cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println(cookie.getName() + ":" + cookie.getValue());
            }
        } else {
            System.out.println("WTF! Cookie is null!");
        }
        return "t1";
    }
}