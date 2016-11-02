package dominus.intg.datastore.cache.redis;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestRedis extends RedisBaseTestCase {

    @Test
    public void testGetSet() {
        assertEquals(SUCCESS_RESPONSE, jedis.set("foo", "bar"));
        assertEquals("bar", jedis.get("foo"));
        assertEquals(0, jedis.setnx("foo", "bar").intValue());
    }

    @Test
    public void testHashValue() {
//        jedis.hset
    }

    @Test
    public void testListValue() {

    }

    /**
     * In Redis 2.4 the expire might not be pin-point accurate, and it could be between zero to one seconds out.
     * Since Redis 2.6 the expire error is from 0 to 1 milliseconds.
     */
    @Test
    public void testKeyExpire() throws InterruptedException {
        jedis.set(uniqueKey, "hello world", NX, EX, 3);
        Thread.sleep(4 * Second);
        assertNull(jedis.get(uniqueKey));
    }

    @Test
    public void testExceedMaxMemory() {

    }

}
