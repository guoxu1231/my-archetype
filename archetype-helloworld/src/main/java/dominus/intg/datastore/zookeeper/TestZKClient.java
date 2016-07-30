package dominus.intg.datastore.zookeeper;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.zookeeper.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestZKClient extends DominusJUnit4TestBase implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        if (event.getPath() == null)
            out.println(event);
        else
            try {
                printf(ANSI_BLUE, "[Watched Event] %s\n%s:%s\n", event.toString(), ACTIVE_NODE_PATH, Arrays.toString(_zkClient.getChildren(ACTIVE_NODE_PATH, true).toArray()));
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    static final String TEST_ROOT_PATH = "/zk-test";
    static final String ACTIVE_NODE_PATH = "/zk-test/active";
    static final int CONN_TIMEOUT = 6000;

    ZooKeeper _zkClient;
    ClientCnxn _clientCnxn;

    Watcher EMPTY_WATCHER = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            //do thing
        }
    };

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        _zkClient = new ZooKeeper(properties.getProperty("zk.connect"), CONN_TIMEOUT, this);
        out.println("[ZK Session ID] :" + _zkClient.getSessionId());
        _clientCnxn = (ClientCnxn) FieldUtils.readDeclaredField(_zkClient, "cnxn", true);
        assertEquals(_zkClient.getSessionId(), _clientCnxn.getSessionId());
        if (_zkClient.exists(TEST_ROOT_PATH, false) == null)
            _zkClient.create(TEST_ROOT_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (_zkClient.exists(ACTIVE_NODE_PATH, false) == null)
            _zkClient.create(ACTIVE_NODE_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        sleep(100);
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        _zkClient.close();
    }

    @Test
    public void testCreateEphemeralNode() throws InterruptedException, KeeperException, IOException {
        ZooKeeper testClient = new ZooKeeper(properties.getProperty("zk.connect"), CONN_TIMEOUT, EMPTY_WATCHER);
        assertEquals("[]", Arrays.toString(_zkClient.getChildren(ACTIVE_NODE_PATH, true).toArray()));
        testClient.create(ACTIVE_NODE_PATH + "/active-node-01", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        sleep(100);
        assertEquals("[active-node-01]", Arrays.toString(_zkClient.getChildren(ACTIVE_NODE_PATH, true).toArray()));
        testClient.close();
        sleep(100);
    }

    @Ignore
    public void testSessionTimeout() throws IllegalAccessException, InterruptedException {
        Thread sendThread = (Thread) FieldUtils.readDeclaredField(_clientCnxn, "sendThread", true);
        sendThread.suspend();
        sleep(CONN_TIMEOUT);
        sendThread.resume();//Unable to reconnect to ZooKeeper service, session 0x15618178233000d has expired, closing socket connection
        sleep(2000);
        assertFalse(_zkClient.getState().isAlive());
    }
}
