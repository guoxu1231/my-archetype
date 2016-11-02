package dominus.intg.message.mq;


import com.aliyun.openservices.ons.api.Producer;
import com.aliyuncs.exceptions.ClientException;
//EE: public cloud & finance cloud package
//import com.aliyuncs.ons4financehz.model.v20160405.OnsTopicStatusResponse;
import dominus.framework.junit.annotation.MessageQueueTest;
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
    @MessageQueueTest(count = 1000)
    @Test
    public void testSimpleMessage() throws InterruptedException, ClientException {
        Integer msgCount = messageQueueAnnotation.count();
        produceTestMessage(producer, testTopicId, msgCount);
    }

}
