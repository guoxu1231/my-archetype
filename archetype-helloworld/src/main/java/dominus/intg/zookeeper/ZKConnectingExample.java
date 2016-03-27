package dominus.intg.zookeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import dominus.RAVI_CDH_CONN;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Basic ZK client connection test:
 *      Enable logging to review heart beat(PING) request;
 *      try session partition tolerance : SyncConnected->Disconnected->Expired(END)
 */
public class ZKConnectingExample {

    private static final int SESSION_TIMEOUT = 50000;
    final static Logger logger = LoggerFactory.getLogger(ZKConnectingExample.class);

    public ZooKeeper connect(String hosts) throws IOException, InterruptedException {
        final CountDownLatch signal = new CountDownLatch(30);

        //SyncConnected->Disconnected->Expired(END)
        ZooKeeper zk = new ZooKeeper(hosts, SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                signal.countDown();
                logger.info("Counting down...." + event);
            }
        });
        signal.await();
        return zk;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ZKConnectingExample example = new ZKConnectingExample();
        ZooKeeper zk = example.connect(RAVI_CDH_CONN.ZK_CONN_STRING);
        System.out.printf("ZK state: %s\n", zk.getState());
        zk.close();
    }
}