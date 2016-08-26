package gladiator.binlog;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

/**
 * select all PK into memory;
 * update records one by one in separate transaction to mock continuous small tx binlog stream;
 */
public class BinLogGeneratorService {

    protected final Logger logger = LoggerFactory.getLogger(BinLogGeneratorService.class);

    final static String LOAD_PK_SQL = "select %s from %s";
    final static String UPDATE_TIMESTAMP_SQL = "update %s set %s=? where %s=?";
    List<Long> pkList;

    JdbcTemplate jdbcTemplate;

    public BinLogGeneratorService(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        logger.info("{} is initialized", dataSource.getClass().getName());
    }

    public String loadPK(String table, String pkColumn) {
        pkList = jdbcTemplate.queryForList(String.format(LOAD_PK_SQL, pkColumn, table), Long.class);

        String echo = String.format("load %s PKs from %s.%s", pkList.size(), table, pkColumn);
        logger.info(echo);
        return echo;
    }

    public String updateTimestamp(String table, String pkColumn, String timestampColumn) {

        if (pkList == null || pkList.size() == 0)
            this.loadPK(table, pkColumn);

        StopWatch stopWatch = new StopWatch(String.format("%s.%s update timestamp in %d records", table, pkColumn, pkList.size()));
        stopWatch.start();
        for (Long pk : pkList) {
            jdbcTemplate.update(String.format(UPDATE_TIMESTAMP_SQL,
                    table, timestampColumn, pkColumn), new Date(), pk);
        }
        stopWatch.stop();
        return stopWatch.toString();
    }

}
