package gladiator.cdc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Profile("gladiator-cdc")
@Configuration
@PropertySource("classpath:properties/data-store.properties")
public class CdcConfig {

    @Autowired
    Environment env;

    @Bean(initMethod = "init", destroyMethod = "cleanup")
    @Description("Run as mysql slave and publish all insert/update/delete events to kafka topics")
    public BinaryLogClientBean binaryLogClientBean() {
        return new BinaryLogClientBean(env.getProperty("mysql.hostname.cdc"), Integer.valueOf(env.getProperty("mysql.port.cdc")), env.getProperty("mysql.schema.cdc"),
                env.getProperty("mysql.username.cdc"), env.getProperty("mysql.password.cdc"));
    }
}
