package dominus.intg.datastore.redis;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.assertEquals;

public class TestRedis extends DominusJUnit4TestBase {

    Jedis jedis;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        jedis = new Jedis(properties.getProperty("redis.connect"));
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        jedis.close();
    }

    @Test
    public void testSetGet() {
        jedis.set("foo", "bar");
        assertEquals("bar", jedis.get("foo"));
    }
}
