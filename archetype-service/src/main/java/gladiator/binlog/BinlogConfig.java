package gladiator.binlog;

import dominus.web.GlobalConfig;
import org.springframework.context.annotation.*;

@Profile("binlog")
@Configuration
public class BinlogConfig extends GlobalConfig {

    @Bean(initMethod = "init", destroyMethod = "cleanup")
    public Object binaryLogParserBean() {

        String host = env.getProperty("mysql.hostname.cdc");
        int port = Integer.valueOf(env.getProperty("mysql.port.cdc"));
        String user = env.getProperty("mysql.username.cdc");
        String password = env.getProperty("mysql.password.cdc");
        Boolean debug = Boolean.valueOf(env.getProperty("binlog.parser.debug"));

        if (env.getProperty("binlog.parser").equalsIgnoreCase("shyiko")) {
            return new BinaryLogClientBean(host, port, user, password, debug);
        } else if (env.getProperty("binlog.parser").equalsIgnoreCase("dbsync")) {
            return new CanalLogFetcherBean(host, port, user, password, debug);
        } else
            return null;
    }
}
