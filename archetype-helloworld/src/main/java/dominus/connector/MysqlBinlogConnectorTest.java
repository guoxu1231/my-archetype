package dominus.connector;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import dominus.intg.datastore.mysql.MySqlZBaseTestCase;
import org.junit.Test;
import org.springframework.jdbc.datasource.init.ScriptParseException;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
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

    private void disconnect() {
        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Prior to MySQL 5.7.7, statement-based format was the default.
    // In MySQL 5.7.7 and later, row-based format is the default.
    @Test
    public void testListenerBinlogEvent() throws IOException, SQLException {
        //EE: binlog event listener
        client.registerEventListener(new BinaryLogClient.EventListener() {
            //EE: Transaction Event Flow: GTID event (if gtid_mode=ON) -> QUERY event with "BEGIN" as sql -> ... -> XID event
            EventType[] expectedEvents = {EventType.QUERY, EventType.QUERY, EventType.QUERY,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_WRITE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_UPDATE_ROWS,
                    EventType.TABLE_MAP, EventType.EXT_DELETE_ROWS,
                    EventType.XID
            };
            Queue<EventType> eventTypeQueue = new LinkedList<EventType>(Arrays.asList(expectedEvents));

            @Override
            public void onEvent(Event event) {

                out.println(event);
                EventType eventType = event.getHeader().getEventType();
                //ignore trivial events
                if (EventType.ROTATE.equals(eventType) || EventType.FORMAT_DESCRIPTION.equals(eventType))
                    return;
                else {
                    assertEquals(eventTypeQueue.poll(), eventType);
                    if (eventTypeQueue.isEmpty()) disconnect();
                }
            }
        });
        client.connect();
        assertTrue(client.isBlocking());
    }
}
