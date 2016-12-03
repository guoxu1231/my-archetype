package dominus.intg.message.kafka;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

public class KafkaIntgTestCase extends KafkaZBaseTestCase {
    JedisPool jedisPool;
    Jedis jedis;

//    final String TEST_TOPIC = "CONSUMER_AUDIT";

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        URI redisURI = new URI(properties.getProperty("redis.connect"));
        jedisPool = new JedisPool(new JedisPoolConfig(), redisURI.getHost(), redisURI.getPort(), Protocol.DEFAULT_TIMEOUT, null, 9);
        jedis = jedisPool.getResource();
        jedis.flushDB();

        this.createTestTopic(testTopicName);
    }

    @Override
    protected void doTearDown() throws Exception {
        this.deleteTestTopic(testTopicName);
        jedisPool.getResource().flushDB();
        jedisPool.close();
        super.doTearDown();
    }

    @Test
    public void testProduceConsumeDiff() throws URISyntaxException, ExecutionException, InterruptedException {
        final String REDIS_LIST_NAME = "CONSUMER_GROUP_ID";
        //consume specific topic and send voucher to audit topic, meantime populate redis audit cache.
        Consumer<String, String> topicReader = this.createDefaultConsumer(SEEDED_TOPIC, "topic-reader-" + System.currentTimeMillis(), null, null);
        executorService.submit(() -> {
            Jedis jedis = jedisPool.getResource();
            Long count = 0L;
            while (true) {
                ConsumerRecords<String, String> records = topicReader.poll(pollTimeout);
                if (!records.isEmpty()) {
                    List<String> simpleRecords = new ArrayList(records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        simpleRecords.add(Long.toString(record.partition()) + ":" + Long.toString(record.offset()));

                    }
                    jedis.sadd(REDIS_LIST_NAME, simpleRecords.toArray(new String[records.count()]));
                    logger.info("add to redis set {}", Arrays.toString(simpleRecords.toArray()));
                    for (ConsumerRecord<String, String> record : records) {
                        _producer.send(new ProducerRecord(testTopicName, String.valueOf(record.partition()), String.valueOf(record.offset())));
                    }
                    count += records.count();
                }
                if (count.equals(SEEDED_TOPIC_COUNT))
                    break;
            }
            logger.info("topic reader exit");
        });

        //fetch consumed messages(? percent random failed) and remove it from redis list
        Consumer<String, String> topicAudit = this.createDefaultConsumer(testTopicName, "topic-audit-" + System.currentTimeMillis(), null, null);
        executorService.submit(() -> {
            Jedis jedis = jedisPool.getResource();
            Long count = 0L;
            while (true) {
                ConsumerRecords<String, String> records = topicAudit.poll(pollTimeout);
                if (!records.isEmpty()) {
                    List<String> simpleRecords = new ArrayList(records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        simpleRecords.add(record.key() + ":" + record.value());
                    }
                    jedis.srem(REDIS_LIST_NAME, simpleRecords.toArray(new String[records.count()]));
                    logger.info("remove from redis set {}", Arrays.toString(simpleRecords.toArray()));
                    count += records.count();
                }
                if (count.equals(SEEDED_TOPIC_COUNT))
                    break;
            }
            logger.info("topic audit exit");
        }).get();
        assertTrue(jedis.smembers(REDIS_LIST_NAME).size() == 0);
    }

}
