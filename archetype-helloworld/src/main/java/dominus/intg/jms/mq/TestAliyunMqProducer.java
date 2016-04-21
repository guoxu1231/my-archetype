package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.ons.model.v20160405.OnsTopicStatusResponse;
import org.junit.Test;


import java.util.Properties;

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
        Thread.sleep(3 * Second);
        this.createProducerPublish(testTopicId, testProducerId);
        Thread.sleep(30 * Second);
        //sleep to wait for topic and publish info updated to name server.
        producer = this.createProducer();
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        this.deleteTestTopic(testTopicId);
        this.deleteProducerPublish(testTopicId, testProducerId);
        producer.shutdown();
    }


    /**
     * send simple message
     */
    @Test
    public void testSimpleMessage() throws InterruptedException, ClientException {
        Long msgCount = Long.valueOf(properties.getProperty("aliyun.mq.testTopic.count"));
        for (int i = 0; i < msgCount; i++) {
            Message msg = new Message(testTopicId, "TagA", "Hello ONS".getBytes());
            // unique business key,can be used to re-send message in case not found in ONS console
            msg.setKey("ORDERID_100");
            //EE: naming server update delay, cause [MQClientFactoryScheduledThread] WARN  RocketmqClient - get Topic [TBW102] RouteInfoFromNameServer is not exist value
            SendResult sendResult = producer.send(msg);
            assert sendResult != null;
            println(ANSI_RED, "send success: " + sendResult);
        }
        OnsTopicStatusResponse.Data data = this.getTopicStatus(testTopicId);
        assertEquals(msgCount, data.getTotalCount());
    }

    /**
     * send transactional message
     */
    @Test
    public void testTransactionalMessage() {

    }

    @Test
    public void testConnectivity() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ProducerId, "PID-D-GUOXU-TEST-20160420-1461146687862");
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        //properties.put(PropertyKeyConst.ONSAddr, ONS_ADDRESS);

        Producer producer = ONSFactory.createProducer(properties);
        producer.start();
        out.printf("ONS Producer is started!%s\n", producer.getClass());

        for (int i = 0; i < 10; i++) {
            Message msg = new Message("D-GUOXU-TEST-20160420-1461146687861", "TagA", "Hello ONS".getBytes());
            // unique business key,can be used to re-send message in case not found in ONS console
            msg.setKey("ORDERID_100");
            SendResult sendResult = producer.send(msg);
            assert sendResult != null;
            println(ANSI_RED, "send success: " + sendResult);
        }

    }
}
