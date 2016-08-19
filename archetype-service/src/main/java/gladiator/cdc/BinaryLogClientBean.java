package gladiator.cdc;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.IOException;

/**
 * Run as mysql slave and publish all insert/update/delete events to kafka topics;
 * Each topic store events for corresponding table.
 * 2016/07/06
 */
public class BinaryLogClientBean {

    final BinaryLogClient client;
    protected final Logger logger = LoggerFactory.getLogger(BinaryLogClientBean.class);

    //EE: update batch records and test binlog parser performance. begin event as ..., end event as ...
    StopWatch stopWatch;

    public BinaryLogClientBean(String hostname, int port, String schema, String username, String password) {
        client = new BinaryLogClient(hostname, port, schema, username, password);
    }

    private void stats(Event event) {

    }

    public void init() throws IOException {
        //EE: prevent blocking main thread during boot initialization.
        Thread t = new Thread() {
            @Override
            public void run() {
                //EE: binlog event listener
                logger.info("Initializing binaryLog client....");
                client.registerEventListener(new BinaryLogClient.EventListener() {

                    @Override
                    public void onEvent(Event event) {

                        if (event.getData() instanceof WriteRowsEventData || event.getData() instanceof UpdateRowsEventData || event.getData() instanceof DeleteRowsEventData) {
                            logger.info(event.toString());
                            //TODO publish to kafka
                        }
                    }
                });
                try {
                    client.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.setName("binaryLog-client-thread");
        t.start();
    }

    public void cleanup() throws IOException {
        client.disconnect();
    }

    public BinaryLogClient getClient() {
        return client;
    }

    static class BinlogParserStopWatch extends StopWatch {

    }
}
