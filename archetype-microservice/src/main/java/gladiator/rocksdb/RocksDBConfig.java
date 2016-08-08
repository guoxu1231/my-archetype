package gladiator.rocksdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Profile("rocksdb")
@Configuration
@PropertySource("classpath:properties/data-store.properties")
public class RocksDBConfig {

    @Autowired
    Environment env;

    @Bean(name = "rocksdb")
    public RocksDBPerfTest rocksDBPerfTest() {
        RocksDBPerfTest rocks = new RocksDBPerfTest();
        rocks.setSourceSql(env.getProperty("rocks.source.sql"));
        rocks.setSourceKey(env.getProperty("rocks.source.key"));
        rocks.setSourceCountSql(env.getProperty("rocks.source.count-sql"));

        return rocks;
    }
}
