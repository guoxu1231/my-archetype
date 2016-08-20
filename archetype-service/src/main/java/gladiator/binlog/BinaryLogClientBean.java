package gladiator.binlog;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    BinlogParserStopWatch stopWatch = new BinlogParserStopWatch();

    public BinaryLogClientBean(String hostname, int port, String username, String password) {
        client = new BinaryLogClient(hostname, port, username, password);
    }

    /**
     * GTID event (if gtid_mode=ON) -> QUERY event with "BEGIN" as sql -> ... -> XID event | QUERY event with "COMMIT" or "ROLLBACK" as sql.
     *
     * @param event
     */
    private void txEventStats(Event event) {
        //Begin stopwatch once 'BEGIN' Query event;Stop stopwatch once 'XID' or 'COMMIT' event;
        if (event.getData() instanceof QueryEventData && ((QueryEventData) event.getData()).getSql().equals("BEGIN")) {
            stopWatch = new BinlogParserStopWatch();
            stopWatch.start("tx-event-stat-" + ((QueryEventData) event.getData()).getDatabase());
            stopWatch.countEvent(1);
            logger.info("StopWatch start...");
        } else if (event.getData() instanceof XidEventData) {
            stopWatch.stop();
            stopWatch.countEvent(1);
            logger.info(stopWatch.toString());
        } else if (event.getData() instanceof WriteRowsEventData)
            stopWatch.countEvent(((WriteRowsEventData) event.getData()).getRows().size());
        else if (event.getData() instanceof UpdateRowsEventData)
            stopWatch.countEvent(((UpdateRowsEventData) event.getData()).getRows().size());
        else if (event.getData() instanceof DeleteRowsEventData)
            stopWatch.countEvent(((DeleteRowsEventData) event.getData()).getRows().size());
        else
            stopWatch.countEvent(1);
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

//                        logger.info(event.toString());

                        txEventStats(event);

//                        if (event.getData() instanceof WriteRowsEventData || event.getData() instanceof UpdateRowsEventData || event.getData() instanceof DeleteRowsEventData) {
//                            logger.info(event.toString());
//                            //TODO publish to kafka
//                        }
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
}
