package dominus.intg.jms.kafka09;


import dominus.framework.junit.annotation.MessageQueueTest;
import kafka.common.MessageFormatter;
import kafka.coordinator.GroupMetadataManager;
import org.apache.commons.lang.time.DateUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KafkaConsumerTestcase extends KafkaZBaseTestCase {

    Producer producer;
    Consumer<String, String> consumer;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        //produce test message according to the test annotation
        if (messageQueueAnnotation != null && messageQueueAnnotation.produceTestMessage()) {
            this.createTestTopic(testTopicName);
            producer = this.createDefaultProducer(null);
            //prepare message
            produceTestMessage(producer, testTopicName, messageQueueAnnotation.count());
            assertEquals(messageQueueAnnotation.count(), sumPartitionOffset(brokerList, testTopicName));
        }
    }

    @Override
    protected void doTearDown() throws Exception {
        if (messageQueueAnnotation != null && messageQueueAnnotation.produceTestMessage()) {
            if (producer != null) producer.close();
            this.deleteTestTopic(testTopicName);
        }
        if (consumer != null) consumer.close();
        super.doTearDown();
    }

    @MessageQueueTest(produceTestMessage = false, count = 10000, queueName = "page_visits_10k")
    @Test
    public void testSimpleConsumer() throws InterruptedException {

        consumer = this.createDefaultConsumer(testTopicName, groupId, null, null);

        long count = 0;
        long todayCount = 0;
        Long startOfDay = DateUtils.truncate(new Date(), Calendar.DATE).getTime();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
            logger.info("kafka consumer received {} records", records.count());
            for (ConsumerRecord<String, String> record : records) {
                logger.info("consumed message [key]={} [partition]={} [offset]={} [timestamp]={}",
                        record.key(), record.partition(), record.offset(), simpleDateFormat.format(record.timestamp()));
                count++;
                if (record.timestamp() > startOfDay)
                    todayCount++;
            }
            consumer.commitSync();
            if (count == messageQueueAnnotation.count()) break;
        }
        assertEquals(messageQueueAnnotation.count(), count);
        assertEquals(messageQueueAnnotation.count() / 2, todayCount);
    }

    @MessageQueueTest(produceTestMessage = true, count = 1000)
    @Test
    public void testCommitByRecord() {

        consumer = this.createDefaultConsumer(testTopicName, groupId, null, null);

        int pollingTime = 0;
        int[] offsetLog = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
            pollingTime++;
            logger.info("[{} th], polling size:{}", pollingTime, records.count());

            //EE:only consume first record of partitions
            for (TopicPartition partition : records.partitions()) {
                List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                logger.info(partition + "-" + partitionRecords.size());
                for (ConsumerRecord<String, String> record : partitionRecords) {
                    logger.info("processed record - {}", record);
                    consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                    //EE: consumer side seek
                    consumer.seek(partition, record.offset() + 1);
//                    assertEquals(record.offset() + 1,consumer.committed(partition).offset()); TODO
                    break;
                }
            }
            for (TopicPartition par : records.partitions())
                offsetLog[par.partition()] = offsetLog[par.partition()] + 1;

            if (sum(offsetLog) == messageQueueAnnotation.count())
                break;
        }
        assertEquals(messageQueueAnnotation.count(), sum(offsetLog));
    }

    @MessageQueueTest(produceTestMessage = true, count = 1000)
    @Test
    public void testMessageOrderInPartition() {
        //EE:only assign partition 0 to consumer
        consumer = this.createDefaultConsumer(testTopicName, groupId, null, Collections.singletonList(new TopicPartition(testTopicName, 0)));

        int expectedOffset = 0;
        while (true) {
            ConsumerRecords records = consumer.poll(pollTimeout);
            Iterator<ConsumerRecord> iterator = records.iterator();
            while (iterator.hasNext()) {
                final ConsumerRecord record = iterator.next();
                logger.info(record.toString());
                //EE: offset from 0 to largest
                assertEquals(expectedOffset++, record.offset());
                if (expectedOffset == testMessageMap.get(0).size())
                    return;
            }
        }
    }

    /**
     * If assign to multiple partition, can not guarantee the partition seek works!
     * Because each polling may fetch records from other partitions.
     */
    @MessageQueueTest(produceTestMessage = true, count = 1000)
    @Test
    public void testRandomSeekInPartition() {
        //EE:only assign partition 0 to consumer
        consumer = this.createDefaultConsumer(testTopicName, groupId, null, Collections.singletonList(new TopicPartition(testTopicName, 0)));

        for (int i = 0; i < 10; i++) {
            KafkaTestMessage expectedMsg = testMessageMap.get(0).get(random.nextInt(testMessageMap.get(0).size()));
            consumer.seek(new TopicPartition(testTopicName, 0), expectedMsg.medadata.offset());
            //EE:random seek to partition first record
            ConsumerRecords<?, ?> records = consumer.poll(pollTimeout);
            out.printf("polling records count:%d\n", records.count());
            ConsumerRecord record = records.isEmpty() ? null : records.iterator().next();

            if (record != null) {
                out.printf("Expected Message:(%s,%s), %s\n", expectedMsg.medadata.partition(), expectedMsg.medadata.offset(), expectedMsg.message);
                out.printf("Actual   Message:(%s,%s), %s\n", record.partition(), record.offset(), record);
                assertEquals(expectedMsg.message.key(), record.key());
                assertEquals(expectedMsg.message.value(), record.value());
            }
        }
    }

    /**
     * test max.poll.records in kafka consumer.
     */
    @MessageQueueTest(produceTestMessage = false, count = 10000, queueName = "page_visits_10k")
    @Test
    public void testPollingRecords() {
        consumer = this.createDefaultConsumer(testTopicName, groupId, null, null);
        final int POLLING_RECORDS = Integer.valueOf(kafkaConsumerProps.getProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG)).intValue();
        long count = 0;

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
            if (records.count() != 0) {
                logger.info("kafka consumer received {} records", records.count());
                assertTrue(records.count() == POLLING_RECORDS || records.count() < 10);
                consumer.commitSync();
                count += records.count();
            }
            if (count == messageQueueAnnotation.count()) break;
        }
        assertEquals(messageQueueAnnotation.count(), count);
    }

    /**
     * Exceed SESSION_TIMEOUT_MS will cause consumer re-balance.
     */
    @MessageQueueTest(produceTestMessage = false, count = 10000, queueName = "page_visits_10k")
    @Test
    public void testDynamicPartitionTimeout() throws InterruptedException {
        Properties prop = new Properties();
        prop.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        consumer = this.createDefaultConsumer(testTopicName, groupId, prop, null);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
            if (records.count() != 0) {
                logger.info("kafka consumer received {} records", records.count());
                assertTrue(records.count() == 1);
                logger.info("sleep for SESSION_TIMEOUT_MS: {}", kafkaConsumerProps.getProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
                Thread.sleep(Long.valueOf(kafkaConsumerProps.getProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG)));
                try {
                    consumer.commitSync();
                } catch (CommitFailedException e) {
                    assertTrue(e.getLocalizedMessage().contains("Commit cannot be completed since the group has already rebalanced and assigned the partitions to another member. This means that the time between subsequent calls to poll() was longer than the configured session.timeout.ms, which typically implies that the poll loop is spending too much time message processing. You can address this either by increasing the session timeout or by reducing the maximum size of batches returned in poll() with max.poll.records"));
                    break;
                }
                assertTrue("SESSION still valid", false);
            }
        }
    }

    /**
     * Manual Partition Assignment, Dynamic partition assignment and
     * consumer group coordination(session.timeout.ms) are disabled.
     *
     * @throws InterruptedException
     */
    @MessageQueueTest(produceTestMessage = false, count = 10000, queueName = "page_visits_10k")
    @Test
    public void testStaticPartition() throws InterruptedException {
        Properties prop = new Properties();
        prop.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        consumer = this.createDefaultConsumer(testTopicName, groupId, prop, Collections.singletonList(new TopicPartition(testTopicName, 0)));
        String sessionTimeoutMS = kafkaConsumerProps.getProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
            if (records.count() != 0) {
                logger.info("kafka consumer received {} records", records.count());
                assertTrue(records.count() == 1);
                logger.info("sleep for SESSION_TIMEOUT_MS plus additional 10 seconds: {}", sessionTimeoutMS + 10 * Second);
                //EE: manual assignment will never session timeout
                Thread.sleep(Long.valueOf(sessionTimeoutMS) + 10 * Second);
                consumer.commitSync();
            }
        }
    }


    /**
     * test consumer the __consumer_offsets topic.
     */
    @MessageQueueTest(produceTestMessage = false, count = 10000, queueName = "page_visits_10k")
    @Test
    public void testConsumerOffsetTopic() throws InterruptedException {

        //EE: consumer thread
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(20 * Second);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Properties properties = new Properties();
                properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
                Consumer consumer = createDefaultConsumer(testTopicName, groupId, properties, null);
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
                    if (!records.isEmpty()) {
                        assertEquals(100, records.count());
                        consumer.commitSync();
                    }
                }
            }
        }.start();


        final CountDownLatch latch = new CountDownLatch(100 * numPartitions);
        //EE: __consumer_offsets thread
        new Thread() {
            @Override
            public void run() {
                MessageFormatter formatter = new GroupMetadataManager.OffsetsMessageFormatter();

                Properties properties = new Properties();
                properties.setProperty(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, "false");
                properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer_offsets_group");
                properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
                properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
                properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
                KafkaConsumer<Byte[], Byte[]> consumer = new KafkaConsumer<>(properties);
                consumer.subscribe(Arrays.asList("__consumer_offsets"));
                //should receive 1000 message commit request;
                while (true) {
                    ConsumerRecords<Byte[], Byte[]> records = consumer.poll(pollTimeout);
                    for (ConsumerRecord record : records) {
                        formatter.writeTo(record, out);
                        latch.countDown();
                        logger.info(String.valueOf(latch.getCount()));
                    }
                    if (latch.getCount() == 0)
                        break;
                }
            }
        }.start();

        assertEquals(true, latch.await(120, TimeUnit.SECONDS));
    }

    @MessageQueueTest(produceTestMessage = false, count = 10000, queueName = "page_visits_10k")
    @Test
    public void testMessageReplayByCutoffDay() throws InterruptedException {

        final Long cutoffTimestamp = DateUtils.truncate(new Date(), Calendar.DATE).getTime();
        final KafkaCommandMessage commandMessage = new KafkaCommandMessage();

        //EE: consumer thread
        final CountDownLatch latch = new CountDownLatch(messageQueueAnnotation.count());
        final CountDownLatch cutoffLatch = new CountDownLatch(messageQueueAnnotation.count());
        final Consumer consumer = createDefaultConsumer(testTopicName, groupId, null, null);
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    ConsumerRecords<String, String> records = null;
                    try {
                        records = consumer.poll(pollTimeout);
                        for (ConsumerRecord record : records) {
                            latch.countDown();
                            if (record.timestamp() > cutoffTimestamp)
                                cutoffLatch.countDown();
                            else {
                                logger.info("ignore record {} {}", record.partition(), record.offset());
                            }
                        }
                        consumer.commitSync();

                    } catch (WakeupException e) {
                        try {
                            //EE: probably get command message and sleep for command executed.
                            Thread.sleep(1000);
                            printf(ANSI_BLUE, "interrupted by instructor consumer - %s\n", e.getClass());
                            //EE: seek to cutoff day
                            consumer.pause(consumer.assignment());
                            printf(ANSI_BLUE, "consumer [%s] is paused\n", groupId);
                            Consumer seekConsumer = createDefaultConsumer(null, String.format("seek-consumer-%s-%d",
                                    testTopicName, new Date().getTime()), null, consumer.assignment());
                            println(ANSI_BLUE, "seeker consumer is created");
                            seekConsumer.seekToBeginning(consumer.assignment());
                            HashMap<TopicPartition, Long> seekResultMap = new HashMap<>();
                            while (true) {
                                ConsumerRecords<String, String> seekRecords = seekConsumer.poll(pollTimeout);
                                if (!seekRecords.isEmpty()) {
                                    for (ConsumerRecord seekRecord : seekRecords) {
                                        if (seekRecord.timestamp() > Long.valueOf(commandMessage.value) &&
                                                !seekResultMap.containsKey(new TopicPartition(seekRecord.topic(), seekRecord.partition()))) {
                                            seekResultMap.put(new TopicPartition(seekRecord.topic(), seekRecord.partition()), seekRecord.offset());
                                            printf(ANSI_BLUE, "partition [%s] re-seek is done, cutoff offset is [%d]\n", seekRecord.partition(), seekRecord.offset());
                                        }
                                    }
                                }
                                if (seekResultMap.keySet().size() == consumer.assignment().size())
                                    break;
                            }
                            seekConsumer.close();
                            println(ANSI_BLUE, "all partition re-seek is done");
                            for (TopicPartition par : seekResultMap.keySet()) {
                                consumer.seek(par, seekResultMap.get(par));
                            }
                            printf(ANSI_BLUE, "consumer [%s] is re-seeked\n", groupId);
                            consumer.resume(consumer.assignment());
                            printf(ANSI_BLUE, "consumer [%s] is resumed\n", groupId);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }.start();
        assertTrue(latch.await(100, TimeUnit.SECONDS));
        printf(ANSI_BLUE, "all %s messages are consumed by %s\n", messageQueueAnnotation.count(), groupId);

        new Thread() {
            @Override
            public void run() {
                final Consumer instructor = createDefaultConsumer(COMMAND_TOPIC, "__consumer_command_request-" + new Date().getTime(), null, null);
                produceCommandMessage(_producer, COMMAND_TOPIC, COMMAND_REPLAY, String.valueOf(DateUtils.truncate(new Date(), Calendar.DATE).getTime()));
                while (true) {
                    ConsumerRecords<String, String> records = instructor.poll(pollTimeout);
                    if (!records.isEmpty()) {
                        for (ConsumerRecord<String, String> commandRecord : records) {
                            if (commandRecord.key().equals(COMMAND_REPLAY)) {
                                printf(ANSI_BLUE, "Instructor consumer get command message:%s %s\n", commandRecord.key(), commandRecord.value());
                                //EE: get command message and wakeup consumer
                                commandMessage.key = commandRecord.key();
                                commandMessage.value = commandRecord.value();
                                consumer.wakeup();
                            } else {
                                logger.info("ignore command message:{}", commandRecord);
                            }
                        }
                    }
                }
            }
        }.start();
        assertTrue(cutoffLatch.await(100, TimeUnit.SECONDS));
    }

}
