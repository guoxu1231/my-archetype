package gladiator.mongodb;


import dominus.web.sparkjava.support.SparkRoute;
import gladiator.mongodb.MongodbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.mongodb.morphia.Datastore;
import gladiator.mongodb.MongodbConfig.*;

import static spark.Spark.get;


@Component
public class SparkBenchmarkController implements SparkRoute {

    @Autowired
    Datastore testDatastore;

    @Override
    public void register() {
        //simple route
        get("/bench/mongo-get", (request, response) -> {
            People people = testDatastore.createQuery(MongodbConfig.People.class).filter("name =", "shawguo").get();
            return people;
        });
    }
}
