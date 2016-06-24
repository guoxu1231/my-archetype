package dominus.connector;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import dominus.intg.datastore.mysql.MySqlZBaseTestCase;
import org.junit.Test;
import org.springframework.jdbc.datasource.init.ScriptParseException;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

public class MysqlBinlogConnectorTest extends MySqlZBaseTestCase {

    BinaryLogClient client;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        client = new BinaryLogClient(properties.getProperty("mysql.hostname"),
                3306, properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));

        //EE: execute sql statements;
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3 * Second);
                    ScriptUtils.executeSqlScript(stageMysqlDS.getConnection(),
                            resourceLoader.getResource("classpath:script/sql/department.sql"));
                } catch (Exception e) {
                    throw new ScriptParseException("department.sql parse error", null, e);
                }
            }
        }.start();
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }

    //Prior to MySQL 5.7.7, statement-based format was the default.
    // In MySQL 5.7.7 and later, row-based format is the default.
    @Test
    public void testListenerBinlogEvent() throws IOException, SQLException {
        //EE: binlog event listener
        client.registerEventListener(new BinaryLogClient.EventListener() {
            int insert = 0, update = 0, delete = 0;

            @Override
            public void onEvent(Event event) {
                out.println(event);
                if (event.getData() instanceof WriteRowsEventData)
                    insert += ((WriteRowsEventData) event.getData()).getRows().size();
                else if (event.getData() instanceof DeleteRowsEventData)
                    delete += ((DeleteRowsEventData) event.getData()).getRows().size();
                else if (event.getData() instanceof UpdateRowsEventData)
                    update += ((UpdateRowsEventData) event.getData()).getRows().size();
                if (insert == 9 && delete == 9 && update == 1) {
                    try {
                        client.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        client.connect();
        assertTrue(client.isBlocking());
    }
}
