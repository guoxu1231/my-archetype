package dominus.web.sparkjava.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.port;
import static spark.Spark.threadPool;

@Configuration
public class SparkConfiguration {

    @Autowired(required = false)
    private List<SparkRoute> sparks = new ArrayList<>();
    @Value("${sparkjava.port}")
    Integer prot;
    @Value("${sparkjava.thread}")
    Integer thread;

    @Bean
    CommandLineRunner sparkRunner() {
        System.out.println("Initializing SparkJava ...");
        //Embedded web server
        port(prot);
        threadPool(thread);

        //auto detect
        return args -> sparks.stream().forEach(spark -> spark.register());
    }

}