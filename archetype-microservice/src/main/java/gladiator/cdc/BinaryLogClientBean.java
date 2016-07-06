package gladiator.cdc;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Run as mysql slave and publish all insert/update/delete events to kafka topics;
 * Each topic store events for corresponding table.
 * 2016/07/06
 */
public class BinaryLogClientBean {

    BinaryLogClient client;
    protected final Logger logger = LoggerFactory.getLogger(BinaryLogClientBean.class);

    public BinaryLogClientBean(String hostname, int port, String schema, String username, String password) {
        client = new BinaryLogClient(hostname, port, schema, username, password);
    }

    public void init() throws IOException {
        //EE: binlog event listener
        logger.info("initialize binaryLog client....");
        client.registerEventListener(new BinaryLogClient.EventListener() {

            @Override
            public void onEvent(Event event) {

                if (event.getData() instanceof WriteRowsEventData || event.getData() instanceof UpdateRowsEventData || event.getData() instanceof DeleteRowsEventData) {
                    logger.info(event.toString());
                    //TODO publish to kafka
                }
            }
        });
        client.connect();
    }

    public void cleanup() throws IOException {
        client.disconnect();
    }
}
