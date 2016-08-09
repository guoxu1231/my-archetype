package gladiator.cdc;

import dominus.web.GlobalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Profile("binlog")
@Configuration
public class CdcConfig extends GlobalConfig {

    @Bean(initMethod = "init", destroyMethod = "cleanup")
    @Description("Run as mysql slave and publish all insert/update/delete events to kafka topics")
    public BinaryLogClientBean binaryLogClientBean() {
        return new BinaryLogClientBean(env.getProperty("mysql.hostname.cdc"), Integer.valueOf(env.getProperty("mysql.port.cdc")), env.getProperty("mysql.schema.cdc"),
                env.getProperty("mysql.username.cdc"), env.getProperty("mysql.password.cdc"));
    }
}
