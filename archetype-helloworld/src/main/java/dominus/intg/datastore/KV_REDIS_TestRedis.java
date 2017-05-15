package dominus.intg.datastore;


import com.google.common.io.Files;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KV_REDIS_TestRedis extends KV_REDIS_RedisBaseTestCase {

    String luaScript;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        luaScript = Files.toString(resourceLoader.getResource("classpath:script/redis_lua_sum_values.lua").getFile(), StandardCharsets.UTF_8);
    }

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
    public void testEvalScript() {
        this.produceTestKVs(100);
        String result = jedis.eval(luaScript).toString();
        assertEquals("4950", result); //0..99
    }

    @Test
    public void testPipeline() {
        this.produceTestKVs(1000);
        this.pipelineProduceTestKVs(1000);
    }

}
