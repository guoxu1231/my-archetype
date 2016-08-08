package gladiator.rocksdb;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Load big table into RocksDB.
 * Test Insert/Query performance.
 */
public class RocksDBPerfTest {

    @Autowired
    JdbcTemplate template;

    protected final Logger logger = LoggerFactory.getLogger(RocksDBPerfTest.class);
    RocksDB rocksDB = null;
    Options options;
    String sourceSql;
    String sourceCountSql;
    String sourceKey;
    ObjectMapper mapper;

    public void setSourceCountSql(String sourceCountSql) {
        this.sourceCountSql = sourceCountSql;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public void setSourceSql(String sourceSql) {
        this.sourceSql = sourceSql;
    }

    public RocksDBPerfTest() {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        options = new Options().setCreateIfMissing(true);
        rocksDB = null;
        try {
            // a factory method that returns a RocksDB instance
            rocksDB = RocksDB.open(options, "/tmp/rocksdb");
            // do something
        } catch (RocksDBException e) {
            // do some error handling
            logger.error(e.toString());
        }

        mapper = new ObjectMapper();// create once, reuse
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"));
    }

    public void load() {
        logger.info("rocksdb.load");
        long count = template.queryForObject(sourceCountSql, Long.class);
        logger.info("total count:{}", count);
        AtomicLong now = new AtomicLong(0);
        BasicRowProcessor rowProcessor = new BasicRowProcessor();




        template.setFetchSize(100);
        template.query(sourceSql, rs -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                mapper.writeValue(out, rowProcessor.toMap(rs));
                rocksDB.put(Integer.valueOf(rs.getInt(sourceKey)).toString().getBytes(), out.toByteArray());
            } catch (IOException | RocksDBException e) {
                e.printStackTrace();
            }
            logger.info("{}/{}, now {}%...", now.incrementAndGet(), count, now.get() * 100 / count);
        });
    }

    public void reload() {
        logger.info("rocksdb.reload");
    }

    public void clean() {
        logger.info("rocksdb.clean");

    }

    public void query(String key) {
        logger.info("rocksdb.query");
    }

}
