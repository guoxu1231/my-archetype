package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.Producer;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.ons.model.v20160503.*;
import dominus.framework.junit.annotation.MessageQueueTest;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import java.util.Date;
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


    /**
     * 注册订阅信息，创建订阅组
     *
     * @throws ClientException
     */
    @Test
    @MessageQueueTest(queueName = "D-FINANCE-ONE-160623")
    public void testOnsSubscriptionCreateRequest() throws ClientException {
        OnsSubscriptionCreateRequest request = new OnsSubscriptionCreateRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(messageQueueAnnotation.queueName());
        request.setConsumerId("CID-D-SHAWGUO-TEST");
        OnsSubscriptionCreateResponse response = iAcsClient.getAcsResponse(request);
        System.out.println(response.getRequestId());
    }

    @Test
    @MessageQueueTest(queueName = "D-FINANCE-ONE-160623")
    public void OnsSubscriptionListRequest() throws ClientException {
        OnsSubscriptionListRequest request = new OnsSubscriptionListRequest();
        /**
         *ONSRegionId是指你需要API访问ONS哪个区域的资源。
         *该值必须要根据OnsRegionList方法获取的列表来选择和配置，因为OnsRegionId是变动的，不能够写固定值
         */
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        OnsSubscriptionListResponse response = iAcsClient.getAcsResponse(request);
        List<OnsSubscriptionListResponse.SubscribeInfoDo> subscribeInfoDoList = response.getData();
        for (OnsSubscriptionListResponse.SubscribeInfoDo subscribeInfoDo : subscribeInfoDoList) {
            if (subscribeInfoDo.getTopic().equalsIgnoreCase(messageQueueAnnotation.queueName()))
                System.out.println(subscribeInfoDo.getId() + "  " +
                        subscribeInfoDo.getChannelId() + "  " +
                        subscribeInfoDo.getChannelName() + "  " +
                        subscribeInfoDo.getOnsRegionId() + "  " +
                        subscribeInfoDo.getRegionName() + "  " +
                        subscribeInfoDo.getOwner() + "  " +
                        subscribeInfoDo.getConsumerId() + "  " +
                        subscribeInfoDo.getTopic() + "  " +
                        subscribeInfoDo.getStatus() + "  " +
                        subscribeInfoDo.getStatusName() + " " +
                        subscribeInfoDo.getCreateTime() + "  " +
                        subscribeInfoDo.getUpdateTime());
        }
    }

    /**
     * 查询指定topic的消息状态
     * 根据用户指定的ONS区域，查询用户所拥有的所有Topic的状态，包括当前的Topic上存在的消息数量，更新时间等信息
     *
     * @throws ClientException
     */
    @MessageQueueTest(queueName = "D-FINANCE-ONE-160623")
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
        System.out.printf("[%s] totalCount %d lastTimeStamp %s\n", currentTopic, data.getTotalCount(), simpleDateFormat.format(data.getLastTimeStamp()));
    }


    @Test
    public void testGenerateUsageReport() throws ClientException {
        OnsTopicListRequest request = new OnsTopicListRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());

        OnsTopicListResponse response = iAcsClient.getAcsResponse(request);
        List<OnsTopicListResponse.PublishInfoDo> publishInfoDoList = response.getData();
        for (OnsTopicListResponse.PublishInfoDo publishInfoDo : publishInfoDoList) {
//            System.out.println(publishInfoDo.getTopic() + "     " + new Date(publishInfoDo.getCreateTime()));
            //EE: get topic status
            OnsTopicStatusRequest statusRequest = new OnsTopicStatusRequest();
            statusRequest.setAcceptFormat(FormatType.JSON);
            statusRequest.setOnsRegionId(ONS_REGION_ID);
            statusRequest.setPreventCache(System.currentTimeMillis());
            statusRequest.setTopic(publishInfoDo.getTopic());

            OnsTopicStatusResponse statusResponse = iAcsClient.getAcsResponse(statusRequest);
            OnsTopicStatusResponse.Data data = statusResponse.getData();
            assertNotNull(data);
            if (data.getTotalCount() != 0)
                continue;

            OnsTrendTopicInputTpsRequest trendRequest = new OnsTrendTopicInputTpsRequest();
            trendRequest.setOnsRegionId(ONS_REGION_ID);
            trendRequest.setPreventCache(System.currentTimeMillis());
            trendRequest.setAcceptFormat(FormatType.JSON);
            trendRequest.setTopic(publishInfoDo.getTopic());
            trendRequest.setBeginTime(DateUtils.addMonths(new Date(), -3).getTime());
            trendRequest.setEndTime(System.currentTimeMillis());
            trendRequest.setPeriod(1440L);
            trendRequest.setType(0);

            OnsTrendTopicInputTpsResponse trendResponse = iAcsClient.getAcsResponse(trendRequest);
            OnsTrendTopicInputTpsResponse.Data tpsData = trendResponse.getData();

            //template code end
            System.out.printf("[%s] 消息总数 %d 创建时间 %s, 最后写入时间 %s\n", publishInfoDo.getTopic(), data.getTotalCount(), DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(publishInfoDo.getCreateTime()),
                    data.getLastTimeStamp() == 0 ? "无数据" : DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(data.getLastTimeStamp()));

//            System.out.println(tpsData.getTitle());
            System.out.println("========================================================");
            for (OnsTrendTopicInputTpsResponse.Data.StatsDataDo stat : tpsData.getRecords()) {
                if (stat.getY() != 0)
                    out.printf("日期: %s, 接收消息总量: %s\n", DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(stat.getX()), stat.getY());
            }

            System.out.println("========================================================\n\n\n\n\n");


        }


    }


    /**
     * 重置消费位点
     * 根据用户指定的订阅组，将当前的订阅组消费位点重置到指定时间戳。一般用于清理堆积消息，或者回溯消费。
     * 有2种方式，一种是清理所有消息，一种是清理消费进度到指定的时间。
     *
     * @throws ClientException
     */
    @MessageQueueTest(queueName = "D-FINANCE-ONE-160623", consumerGroupId = "CID-D-SHAWGUO-TEST")
    @Test
    public void testResetConsumerOffset() throws ClientException {
        OnsConsumerResetOffsetRequest request = new OnsConsumerResetOffsetRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setConsumerId(testConsumerId);
        request.setTopic(messageQueueAnnotation.queueName());
        request.setType(1);
        request.setResetTimestamp(System.currentTimeMillis() - 5000 * Minute);

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
     * <p>
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
            if (publishInfoDo.getTopic().contains("D-FINANCE-ONE"))
                System.out.println(publishInfoDo.getTopic() + "     " + new Date(publishInfoDo.getCreateTime()));
        }
    }

//    @Test
//    public void testPurgeTopic() throws ClientException {
//        OnsTopicListRequest request = new OnsTopicListRequest();
//        request.setOnsRegionId(ONS_REGION_ID);
//        request.setPreventCache(System.currentTimeMillis());
//
//        OnsTopicListResponse response = iAcsClient.getAcsResponse(request);
//        List<OnsTopicListResponse.PublishInfoDo> publishInfoDoList = response.getData();
//        for (OnsTopicListResponse.PublishInfoDo publishInfoDo : publishInfoDoList) {
//            System.out.println(publishInfoDo.getTopic() + "     " + new Date(publishInfoDo.getCreateTime()));
//
//
//            OnsTopicDeleteRequest deleteRequest = new OnsTopicDeleteRequest();
//            deleteRequest.setPreventCache(System.currentTimeMillis());
//            deleteRequest.setOnsRegionId(ONS_REGION_ID);
//            deleteRequest.setTopic(publishInfoDo.getTopic());
//            OnsTopicDeleteResponse deleteResponse = null;
//            try {
//                deleteResponse = iAcsClient.getAcsResponse(deleteRequest);
//                System.out.printf("[AliyunMq TestTopic] %s is deleted!\nRequestId=%s, HelpUrl=%s\n", publishInfoDo.getTopic(), deleteResponse.getRequestId(), deleteResponse.getHelpUrl());
//            } catch (ClientException e) {
//                System.out.printf("[AliyunMq TestTopic] %s failed to be deleted!\n", publishInfoDo.getTopic());
//                e.printStackTrace();
//            }
//        }
//    }


    @MessageQueueTest(queueName = "D-GUOXU-TEST-1K-0524", count = 1000)
    @Test
    public void testOnsTrendTopicInputTpsRequest() throws ClientException {

        String currentTopic = messageQueueAnnotation.queueName();

        OnsTrendTopicInputTpsRequest request = new OnsTrendTopicInputTpsRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(currentTopic);
        request.setBeginTime(DateUtils.addMonths(new Date(), -3).getTime());
        request.setEndTime(System.currentTimeMillis());
        request.setPeriod(1L);
        request.setType(0);

        OnsTrendTopicInputTpsResponse response = iAcsClient.getAcsResponse(request);
        OnsTrendTopicInputTpsResponse.Data data = response.getData();
        System.out.println(data.getTitle());
        for (OnsTrendTopicInputTpsResponse.Data.StatsDataDo stat : data.getRecords()) {
            if (stat.getY() != 0)
                out.printf("X: %s, Y: %s\n", new Date(stat.getX()), stat.getY());
        }
    }

    @MessageQueueTest(queueName = "D-FINANCE-ONE-160623")
    @Test
    public void testOnsTrendGroupOutputTpsRequest() throws ClientException {

        String currentTopic = messageQueueAnnotation.queueName();

        OnsTrendGroupOutputTpsRequest request = new OnsTrendGroupOutputTpsRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(currentTopic);
        request.setConsumerId("CID-D-FINANCE-ONE-ACCOUNT-160623");
        request.setBeginTime(System.currentTimeMillis() - 4 * 3600 * 1000);
        request.setEndTime(System.currentTimeMillis());
        request.setPeriod(1L);
        request.setType(0);
        OnsTrendGroupOutputTpsResponse response = iAcsClient.getAcsResponse(request);
        OnsTrendGroupOutputTpsResponse.Data data = response.getData();
        System.out.println(data.getTitle());
        for (OnsTrendGroupOutputTpsResponse.Data.StatsDataDo stat : data.getRecords()) {
            if (stat.getY() != 0)
                out.println(simpleDateFormat.format(stat.getX()) + ", " + stat.getY());
        }
    }


    @MessageQueueTest(queueName = "D-GUOXU-TEST-20K-0620", count = 20000)
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

    @MessageQueueTest(queueName = "D-GUOXU-TEST-20K-0620")
    @Test
    public void deleteTestQueue() {
        this.deleteTestTopic(messageQueueAnnotation.queueName());
    }
}
