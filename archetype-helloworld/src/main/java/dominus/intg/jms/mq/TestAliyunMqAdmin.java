package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.Producer;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.ons.model.v20160503.*;
import dominus.framework.junit.annotation.MessageQueueTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

//EE: public cloud & finance cloud package
//import com.aliyuncs.ons4financehz.model.v20160405.*;

/**
 * 注意：因为POP网关是面对公网环境提供服务的，因此使用Open-API的前提是，客户端需要能够访问公网服务。否则会提示服务无法连接
 */
public class TestAliyunMqAdmin extends TestAliyunMqZBaseTestCase {

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
    }

    @MessageQueueTest(queueName = "D-GUOXU-TEST-1K-0520")
    @Test
    public void testOnsTopicStatusRequest() throws ClientException {

        String currentTopic = messageQueueAnnotation.queueName();

        //template code begin
        OnsTopicStatusRequest request = new OnsTopicStatusRequest();
        request.setAcceptFormat(FormatType.JSON);
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setTopic(currentTopic);
        OnsTopicStatusResponse response = iAcsClient.getAcsResponse(request);
        OnsTopicStatusResponse.Data data = response.getData();
        assertNotNull(data);
        //template code end
        System.out.printf("[%s] totalCount %d lastTimeStamp %d\n", currentTopic, data.getTotalCount(), data.getLastTimeStamp());
    }

    @MessageQueueTest(queueName = "D-GUOXU-TEST-1K-0524", consumerGroupId = "CID-D-GUOXU-TEST-1K-05241")
    @Test
    public void testResetConsumerOffset() throws ClientException {
        OnsConsumerResetOffsetRequest request = new OnsConsumerResetOffsetRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setConsumerId(testConsumerId);
        request.setTopic(messageQueueAnnotation.queueName());
        request.setType(1);
        request.setResetTimestamp(System.currentTimeMillis() - 50 * Minute);

        OnsConsumerResetOffsetResponse response = iAcsClient.getAcsResponse(request);
        System.out.println(response.getRequestId());

    }


    @MessageQueueTest(queueName = "D-GUOXU-TEST-100K")
    @Test
    public void testOnsTopicGetRequest() throws ClientException {

        String currentTopic = messageQueueAnnotation.queueName();

        OnsTopicGetRequest request = new OnsTopicGetRequest();
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(currentTopic);
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());

        OnsTopicGetResponse response = iAcsClient.getAcsResponse(request);
        List<OnsTopicGetResponse.PublishInfoDo> publishInfoDoList = response.getData();
        for (OnsTopicGetResponse.PublishInfoDo publishInfoDo : publishInfoDoList) {
            System.out.println(publishInfoDo.getId() + "  " +
                    publishInfoDo.getChannelId() + "  " +
                    publishInfoDo.getChannelName() + "  " +
                    publishInfoDo.getOnsRegionId() + "  " +
                    publishInfoDo.getRegionName() + "  " +
                    publishInfoDo.getTopic() + "  " +
                    publishInfoDo.getOwner() + "  " +
                    publishInfoDo.getRelation() + "  " +
                    publishInfoDo.getRelationName() + "  " +
                    publishInfoDo.getStatus() + "  " +
                    publishInfoDo.getStatusName() + "  " +
                    publishInfoDo.getAppkey() + "  " +
                    publishInfoDo.getCreateTime() + "  " +
                    publishInfoDo.getUpdateTime() + "  " +
                    publishInfoDo.getRemark());
        }
        System.out.println(response.getRequestId());


    }

    /**
     * 1  cn-qingdao-publictest  公网测试  null    1411623866000  1411623866000
     * 3  cn-qingdao  华北 1 (青岛)  null    1411623866000  1411623866000
     * 4  cn-shenzhen  华南 1 (深圳)  null    1411623866000  1411623866000
     * 6  cn-hangzhou  华东 1 (杭州)  null    1413877180000  1413877180000
     * 8  cn-beijing  华北 2 (北京)  null    1413008259000  1413008259000
     * 9  cn-shanghai  华东 2 (上海)  null    1452840680000  1452840688000
     * <p/>
     * EE:financial private cloud
     * 1  finance-cn-hangzhou  华东 1 (杭州)  null    1417934380000  1417934380000
     * 2  finance-cn-shenzhen  华南 1 (深圳)  null    1456497165000  1456497165000
     *
     * @throws ClientException
     */
    @Test
    public void testOnsRegionListRequest() throws ClientException {
        OnsRegionListRequest request = new OnsRegionListRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setAcceptFormat(FormatType.JSON);
        request.setPreventCache(System.currentTimeMillis());

        OnsRegionListResponse response = iAcsClient.getAcsResponse(request);
        List<OnsRegionListResponse.RegionDo> regionDoList = response.getData();
        for (OnsRegionListResponse.RegionDo regionDo : regionDoList) {
            System.out.println(regionDo.getId() + "  " +
                    regionDo.getOnsRegionId() + "  " +
                    regionDo.getRegionName() + "  " +
                    regionDo.getChannelId() + "  " +
                    regionDo.getChannelName() + "  " +
                    regionDo.getCreateTime() + "  " +
                    regionDo.getUpdateTime());
        }
    }

    @Test
    public void testListMyTopic() throws ClientException {
        OnsTopicListRequest request = new OnsTopicListRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());

        OnsTopicListResponse response = iAcsClient.getAcsResponse(request);
        List<OnsTopicListResponse.PublishInfoDo> publishInfoDoList = response.getData();
        for (OnsTopicListResponse.PublishInfoDo publishInfoDo : publishInfoDoList) {
            if (publishInfoDo.getTopic().contains("GUOXU"))
                System.out.println(publishInfoDo.getTopic() + "     " + publishInfoDo.getOwner());
        }
    }

    @MessageQueueTest(queueName = "D-GUOXU-TEST-1K-0524", count = 1000)
    @Test
    public void testOnsTrendTopicInputTpsRequest() throws ClientException {

        String currentTopic = messageQueueAnnotation.queueName();

        OnsTrendTopicInputTpsRequest request = new OnsTrendTopicInputTpsRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(currentTopic);
        request.setBeginTime(System.currentTimeMillis() - 20 * Minute);
        request.setEndTime(System.currentTimeMillis());
        request.setPeriod(1L);
        request.setType(1);

        OnsTrendTopicInputTpsResponse response = iAcsClient.getAcsResponse(request);
        OnsTrendTopicInputTpsResponse.Data data = response.getData();
        System.out.println(data.getTitle());
        for (OnsTrendTopicInputTpsResponse.Data.StatsDataDo stat : data.getRecords()) {
            out.printf("X: %s, Y: %s\n", stat.getX(), stat.getY());
        }
    }

    @MessageQueueTest(queueName = "D-GUOXU-TEST-1K-0524", count = 1000)
    @Test
    public void createTestQueue() throws ClientException, IllegalAccessException, InterruptedException {
        String queueName = messageQueueAnnotation.queueName();
        int count = messageQueueAnnotation.count();
        String publishId = "PID-" + queueName;

        this.createTestTopic(queueName);
        this.createProducerPublish(queueName, publishId);
        Producer producer = this.createProducer(publishId);
        produceTestMessage(producer, queueName, count);
        this.deleteProducerPublish(queueName, publishId);
    }

    @MessageQueueTest(queueName = "D-GUOXU-TEST-1K-0520", count = 1000)
    @Test
    public void deleteTestQueue() {
        this.deleteTestTopic(messageQueueAnnotation.queueName());
    }
}
