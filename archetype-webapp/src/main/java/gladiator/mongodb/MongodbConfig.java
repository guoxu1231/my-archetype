package gladiator.mongodb;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import dominus.web.GlobalConfig;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
@Configuration
public class MongodbConfig extends GlobalConfig {

    @Autowired
    Mongo mongo;
    @Value("${spring.data.mongodb.database}")
    String testDb;

    @Bean
    public Datastore morphia() {
        final Morphia morphia = new Morphia();
        morphia.mapPackage("gladiator.mongodb");
        Datastore assetDatastore = morphia.createDatastore((MongoClient) mongo, testDb);
        return assetDatastore;
    }

    @Entity("people")
    public static class People {
        @Id
        private String id;
        private String name;
        private String phone;

        @Override
        public String toString() {
            return "People{" +
                    "name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    '}';
        }
    }

}
