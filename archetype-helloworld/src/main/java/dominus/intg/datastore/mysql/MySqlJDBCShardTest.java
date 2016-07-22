package dominus.intg.datastore.mysql;


import org.apache.kafka.common.utils.Utils;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Sharding Pinterest: How we scaled our MySQL fleet
 * EE: Hash Slot Partition
 */
public class MySqlJDBCShardTest extends MySqlZBaseTestCase {

    static final String SHARDING_TABLE = "EMPLOYEE";
    static final int SHARDING_SIZE = 3;
    static final int SHARDING_HASH_SLOT = 16384;
    static final short TYPE_ID = 1;
    JdbcTemplate stageTemplate;
    JdbcTemplate ddlTemplate;
    MySQLMaxValueIncrementer incrementer;
    Map<Integer, String> shardDBMap = new HashMap<>();
    static final Map<HashSlot, Integer> HASH_SLOT_MAP = new HashMap<>();

    {
        HASH_SLOT_MAP.put(new HashSlot(0, 2000), 0);
        HASH_SLOT_MAP.put(new HashSlot(2001, 8000), 1);
        HASH_SLOT_MAP.put(new HashSlot(8001, SHARDING_HASH_SLOT), 2);
    }

    static class HashSlot {
        public HashSlot(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        int begin;
        int end;

        public boolean isBetween(int n) {
            return (n >= begin && n <= end);
        }
    }

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        stageTemplate = new JdbcTemplate(stageMysqlDS);
        ddlTemplate = new JdbcTemplate(ddlDS);
        stageTemplate.execute(String.format("CREATE TABLE IF NOT EXISTS %s_SEQUENCE (VALUE INT NOT NULL) ENGINE=MYISAM;", SHARDING_TABLE));
        stageTemplate.execute(String.format("INSERT INTO %s_SEQUENCE VALUES(0);", SHARDING_TABLE));
        incrementer = new MySQLMaxValueIncrementer(stageMysqlDS, SHARDING_TABLE + "_SEQUENCE", "VALUE");
        incrementer.setCacheSize(100);
        //EE: local id incrementer
        assertEquals(1, incrementer.nextIntValue());
        for (int i = 0; i < SHARDING_SIZE; i++) {
            String shardDatabaseName = "SHARD_" + SHARDING_TABLE + "_" + i;
            ddlTemplate.execute("CREATE DATABASE IF NOT EXISTS " + shardDatabaseName);
            ddlTemplate.execute(String.format("CREATE TABLE IF NOT EXISTS %s.employees LIKE employees.employees", shardDatabaseName));
            ddlTemplate.execute(String.format("ALTER TABLE %s.employees ADD COLUMN GLOBAL_ID int not null;", shardDatabaseName));
            shardDBMap.put(i, shardDatabaseName);
        }
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        stageTemplate.execute(String.format("DROP TABLE IF EXISTS %s_SEQUENCE", SHARDING_TABLE));
        for (int i = 0; i < 3; i++) {
            String shardDatabaseName = "SHARD_" + SHARDING_TABLE + "_" + i;
            ddlTemplate.execute("DROP DATABASE IF EXISTS " + shardDatabaseName);
        }
    }

    @Test
    public void testShardEmployee() {
        ddlTemplate.query("select * from employees.employees limit 1000", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int localId = incrementer.nextIntValue();

                Integer emp_no = rs.getInt("emp_no");
                short shardId = new Integer(Utils.murmur2(new byte[]{
                        (byte) (emp_no >>> 24),
                        (byte) (emp_no >>> 16),
                        (byte) (emp_no >>> 8),
                        emp_no.byteValue()
                }) & 0x7fffffff % SHARDING_HASH_SLOT).shortValue();
                int partition = 0;
                for (HashSlot slot : HASH_SLOT_MAP.keySet())
                    if (slot.isBetween(shardId))
                        partition = HASH_SLOT_MAP.get(slot);
                logger.info("[emp_no]:{}, [shardID]:{} [partition]:{}", emp_no, shardId, partition);

                /**
                 * We created a 64 bit ID that contains the shard ID, the type of the containing data, and where this data is in the table (local ID).
                 * The shard ID is 16 bits, type ID is 10 bits and local ID is 36 bits.
                 */
                long globalId = (shardId << 46) | (TYPE_ID << 36) | (localId << 0);
                ddlTemplate.update(String.format("insert into %s.employees (emp_no,birth_date,first_name,last_name,gender,hire_date,GLOBAL_ID) " +
                                "values(?,?,?,?,?,?,?);", shardDBMap.get(partition)), rs.getInt("emp_no"), rs.getDate("birth_date"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("gender"), rs.getDate("hire_date"), globalId);
            }
        });
    }

}
