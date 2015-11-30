package dominus.middleware.zookeeper.recipes.cluster_management;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import dominus.RAVI_CDH_CONN;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Recipe #1: Cluster Management
 *
 * Each Client Host i, i:=1 .. N
 *      Watch on  /members
 *      Create /members/host-${i} as ephemeral nodes
 *      Node Join/Leave generates alert
 *      Keep updating /members/host-${i} periodically for node status changes(load, memory, CPU etc.)
 */
public class StatusWatcher extends Thread implements Watcher {

    private boolean stopped = false;
    private ZooKeeper zk;
    private final String STATUS_UPDATE_PATH = "/MyZK/Recipes/StatusUpdater";
    private String id;


    public StatusWatcher() {

        int SESSION_TIMEOUT = 20000;
        try {
            zk = new ZooKeeper(RAVI_CDH_CONN.ZK_CONN_STRING, SESSION_TIMEOUT, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //disable <logger name="org.apache.zookeeper" level="ALL" /> slf4j logging
        Logger root = (Logger) LoggerFactory.getLogger("org.apache.zookeeper");
        root.setLevel(Level.INFO);

        StatusWatcher watcher = new StatusWatcher();
        watcher.run();
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                List<String> child = zk.getChildren(STATUS_UPDATE_PATH, this);
                Thread.sleep(5000);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println(event);
        try {
            List<String> child = zk.getChildren(STATUS_UPDATE_PATH, false);
            System.out.print("Active Host [");
            for (int i = 0; i < child.size(); i++) {
                System.out.print(child.get(i) + "\t");
            }
            System.out.println("]");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println(event.)
    }

}
