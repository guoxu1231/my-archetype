package dominus.web.sparkjava;

import dominus.web.sparkjava.support.DummyService;
import dominus.web.sparkjava.support.SparkRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static spark.Spark.get;
import static spark.Spark.post;

@Component
@Order(value = 1)
public class HelloSpark implements SparkRoute {

    @Autowired
    DummyService service;

    @Override
    public void register() {
        System.out.println("register spark routes...");

        //simple route
        get("/spark/hello", (request, response) -> "hello world");

        //service autowired
        get("/spark/dummy", (request, response) -> {
            return service.echo("shawguo");
        });

        //query parameters & post body, and response transformer
        post("/spark/request", (request, response) -> {
            System.out.println("spark.Request.queryParams / id=" + request.queryParams("id"));
            System.out.println("spark.Request.queryParams / name=" + request.queryParams("name"));
            System.out.println("spark.Request.body / " + request.body());

            return "hello world";
        });


    }

}