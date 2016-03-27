package dominus.intg.zookeeper.recipes.cluster_management;


import dominus.RAVI_CDH_CONN;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class StatusUpdater extends Thread implements Watcher {

    private boolean stopped = false;
    private ZooKeeper zk;
    private final String STATUS_UPDATE_PATH = "/MyZK/Recipes/StatusUpdater";
    private String id;

    public StatusUpdater() {
        int SESSION_TIMEOUT = 20000;
        try {
            zk = new ZooKeeper(RAVI_CDH_CONN.ZK_CONN_STRING, SESSION_TIMEOUT, this);
            createZKNode(zk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean createZKNode(ZooKeeper zk) {
        id = "HOST-" + ThreadLocalRandom.current().nextInt(10);
        try {
            zk.create(STATUS_UPDATE_PATH + "/" + id, id.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            if (e instanceof KeeperException && ((KeeperException) e).code().equals(KeeperException.Code.NODEEXISTS)) {
                createZKNode(zk);
            }
        }
        return true;
    }

    protected void close() {
        System.out.println("Closing ZooKeeper client....");
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final StatusUpdater updater = new StatusUpdater();

        Executors.newFixedThreadPool(1).submit(updater);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Inside Add Shutdown Hook");
                updater.close();
            }
        });
    }

    @Override
    public void run() {
        while (!stopped) {

            String status = "HELLO_WORLD";

            try {
                zk.setData(STATUS_UPDATE_PATH + "/" + id, status.getBytes(), -1);
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

    }
}
