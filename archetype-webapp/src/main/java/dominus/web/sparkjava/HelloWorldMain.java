package dominus.web.sparkjava;

import static spark.Spark.*;

public class HelloWorldMain {
    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello World");
    }
}