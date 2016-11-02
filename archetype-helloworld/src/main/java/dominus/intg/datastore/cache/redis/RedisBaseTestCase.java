package dominus.intg.datastore.cache.redis;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.springframework.util.StopWatch;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.util.Date;
import java.util.Random;

public class RedisBaseTestCase extends DominusJUnit4TestBase {

    Jedis jedis;
    String uniqueKey;
    static final String NX = "NX";//Only set the key if it does not already exist.
    static final String XX = "XX";//Only set the key if it already exist.
    static final String EX = "EX";//Set the specified expire time, in seconds.
    static final String PX = "PX";//Set the specified expire time, in milliseconds.
    static final String SUCCESS_RESPONSE = "OK";

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        //EE: connection timeout=2s         read timeout=2s
        jedis = new Jedis(new URI(properties.getProperty("redis.connect")), Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
        uniqueKey = simpleDateFormat.format(new Date());
        out.println(jedis.info());
        jedis.select(10); //database id
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        jedis.close();
    }

    public void produceTestKVs(long count) {

        Random rnd = new Random();
        StopWatch watch = new StopWatch("[Producer] message count:" + count);
        watch.start();
        for (long nEvents = 0; nEvents < count; nEvents++) {
            long runtime = new Date().getTime();
            String ip = "192.168.2." + nEvents;
            String info = runtime + ",www.example.com," + ip;
            logger.info("jedis set [key]:{}, [value]:{}, [status]:{}", ip, info, jedis.set(ip, info));
        }
        watch.stop();
        System.out.println(watch);
    }
}
