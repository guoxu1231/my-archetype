package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyuncs.exceptions.ClientException;
//EE: public cloud & finance cloud package
//import com.aliyuncs.ons4financehz.model.v20160405.OnsTopicStatusResponse;
import com.aliyuncs.ons.model.v20160503.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * From Aliyun User manual
 */
public class TestAliyunMqProducer extends TestAliyunMqZBaseTestCase {

    Producer producer;


    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        this.createTestTopic(testTopicId);
        this.createProducerPublish(testTopicId, testProducerId);
        producer = this.createProducer(testProducerId);
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        this.deleteTestTopic(testTopicId);
        this.deleteProducerPublish(testTopicId, testProducerId);
        if (producer != null) producer.shutdown();
    }


    /**
     * Send simple message
     * EE: Guarantied message durability, no lost message.
     * EE: Producer TPS TODO
     * EE: 10 or 100 million level message produce TODO
     */
    @Test
    public void testSimpleMessage() throws InterruptedException, ClientException {
        Integer msgCount = Integer.valueOf(properties.getProperty("aliyun.mq.testTopic.count"));
        produceTestMessage(producer, testTopicId, msgCount);
    }

    /**
     * send transactional message
     */
    @Test
    public void testTransactionalMessage() {

    }


    @Test
    public void testOrderMessage() {
        //send 1 billion ordered message, verify it in consumer

    }

    @Test
    public void testMessageRetry() {
        //send 1 billion ordered message, verify it in consumer

    }

    /**
     * EE: Sync TPS VS ASync TPS
     */
    @Test
    public void testAsyncMessage() {
        //send 1 billion ordered message, verify it in consumer

    }

}
