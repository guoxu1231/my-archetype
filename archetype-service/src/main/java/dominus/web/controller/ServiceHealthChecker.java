package dominus.web.controller;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import gladiator.cdc.BinaryLogClientBean;
import gladiator.rocksdb.RocksDBPerfTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.Assert.assertTrue;

@RestController
public class ServiceHealthChecker {

    protected final Logger logger = LoggerFactory.getLogger(ServiceHealthChecker.class);
    @Autowired
    private ApplicationContext context;

    @Value("${spring.profiles.active}")
    private String activeProfiles;


    @RequestMapping("/health")
    @ResponseBody
    String healthCheck() {
        logger.info("[{}] prepare to start service health check...", activeProfiles);
        assertTrue(context != null);
        try {
            //EE: groovy web console
            HttpResponse<String> jsonResponse = Unirest.post("http://localhost:8090/rest/script")
                    .header("accept", "application/json")
                    .field("script", "9*9")
                    .asString();

            System.out.println(jsonResponse.getBody());
            assertTrue(jsonResponse.getBody().contains("\"result\":\"81\""));

            //EE: mysql binlog
            if (activeProfiles.contains("binlog")) {
                BinaryLogClientBean binaryLogClientBean = context.getBean(BinaryLogClientBean.class);
                logger.info("binlog file:{} position:{}", binaryLogClientBean.getClient().getBinlogFilename(),
                        binaryLogClientBean.getClient().getBinlogPosition());
                assertTrue(StringUtils.hasLength(binaryLogClientBean.getClient().getBinlogFilename()));
            }
            //EE: rocksdb test
            if (activeProfiles.contains("rocksdb")) {
                RocksDBPerfTestService rocksDBPerfTest = context.getBean(RocksDBPerfTestService.class);
                String sourceCountSql = context.getEnvironment().getProperty("rocks.source.count-sql");
                long count = rocksDBPerfTest.template.queryForObject(sourceCountSql, Long.class);
                logger.info("{} count={}", sourceCountSql, count);
                assertTrue(count > 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return "OK";


    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
