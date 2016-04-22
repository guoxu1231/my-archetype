package dominus.intg.jms.mq;


import com.aliyun.openservices.ons.api.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
//EE: public cloud & finance cloud package
import com.aliyuncs.ons4financehz.model.v20160405.*;
//import com.aliyuncs.ons.model.v20160405.*;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.intg.jms.mq.endpoint.DemoMessageListener;
import dominus.intg.jms.mq.endpoint.ResumeMessageListener;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;

public class TestAliyunMqZBaseTestCase extends DominusJUnit4TestBase {

    String ACS_REGION_ID;
    String ONS_REGION_ID;
    String accessKey;
    String secretKey;
    String productName;
    String domain;
    String endpoint;
    String ONS_ADDRESS;
    IAcsClient iAcsClient;
    //EE:financial private cloud setting

    String testTopicId;
    String testProducerId;
    String testConsumerId;

    protected boolean isPublicTest() {
        return ONS_REGION_ID.contains("publictest");
    }

    @Override
    protected void doSetUp() throws Exception {
        accessKey = properties.getProperty("aliyun.accessKey");
        secretKey = properties.getProperty("aliyun.secretKey");
        productName = properties.getProperty("aliyun.mq.product");
        domain = properties.getProperty("aliyun.mq.domain");
        endpoint = properties.getProperty("aliyun.mq.endpoint");
        ACS_REGION_ID = properties.getProperty("aliyun.acs.region");
        ONS_REGION_ID = properties.getProperty("aliyun.mq.region");
        ONS_ADDRESS = properties.getProperty("aliyun.mq.address");
        //create Acs iAcsClient
        DefaultProfile.addEndpoint(endpoint, ACS_REGION_ID, productName, domain);
        IClientProfile profile = DefaultProfile.getProfile(ACS_REGION_ID, accessKey, secretKey);
        iAcsClient = new DefaultAcsClient(profile);

        Date createDate = new Date();
        testTopicId = String.format("D-GUOXU-TEST-%1$tY%1$tm%1$td-%1$TQ", createDate);
        testProducerId = String.format("PID-D-GUOXU-TEST-%1$tY%1$tm%1$td-%1$TQ", createDate);
        testConsumerId = String.format("CID-D-GUOXU-TEST-%1$tY%1$tm%1$td-%1$TQ", createDate);

        out.printf("[ONS_REGION_ID] %s [PRODUCT] %s\n", ONS_REGION_ID, productName);
    }

    @Override
    protected void doTearDown() throws Exception {
    }

    @Test
    public void testNull() throws InterruptedException, ClientException {

        this.createTestTopic(testTopicId);
        Thread.sleep(3000);
        this.createProducerPublish(testTopicId, testProducerId);
        Thread.sleep(3000);

    }

    protected boolean createTestTopic(String testTopicName) throws ClientException, InterruptedException {
        OnsTopicCreateRequest request = new OnsTopicCreateRequest();
//        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(testTopicName);
//        request.setQps(1000l);
        request.setRemark("DEV");
        request.setStatus(0);
        request.setOnsRegionId(ONS_REGION_ID);
//        request.setCluster("DEV-Test");
        request.setPreventCache(System.currentTimeMillis());

        OnsTopicCreateResponse response = iAcsClient.getAcsResponse(request);
        System.out.printf("[AliyunMq TestTopic] %s is created!\nRequestId=%s, HelpUrl=%s\n", testTopicName, response.getRequestId(), response.getHelpUrl());

        //EE: wait for initialization
        if (isPublicTest()) {
            out.println("sleep 30 seconds to wait for initialization");
            Thread.sleep(30 * Second);
        } else {
            out.println("sleep 3 seconds to wait for initialization");
            Thread.sleep(3 * Second);
        }
        return true;
    }

    protected boolean deleteTestTopic(String testTopicName) {
        OnsTopicDeleteRequest request = new OnsTopicDeleteRequest();
        request.setPreventCache(System.currentTimeMillis());
        request.setOnsRegionId(ONS_REGION_ID);
        request.setTopic(testTopicName);
        OnsTopicDeleteResponse response = null;
        try {
            response = iAcsClient.getAcsResponse(request);
            System.out.printf("[AliyunMq TestTopic] %s is deleted!\nRequestId=%s, HelpUrl=%s\n", testTopicName, response.getRequestId(), response.getHelpUrl());
        } catch (ClientException e) {
            System.out.printf("[AliyunMq TestTopic] %s failed to be deleted!\n", testTopicName);
            e.printStackTrace();
        }
        return true;
    }

    protected OnsTopicStatusResponse.Data getTopicStatus(String testTopic) throws ClientException {
        OnsTopicStatusRequest request = new OnsTopicStatusRequest();
        request.setAcceptFormat(FormatType.JSON);
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setTopic(testTopic);

        OnsTopicStatusResponse response = iAcsClient.getAcsResponse(request);
        OnsTopicStatusResponse.Data data = response.getData();
        System.out.printf("[%s] totalCount %d lastTimeStamp %d\n", testTopic, data.getTotalCount(), data.getLastTimeStamp());
        return data;
    }

    protected boolean createProducerPublish(String testTopicId, String testPublishId) throws ClientException, InterruptedException {

        OnsPublishCreateRequest request = new OnsPublishCreateRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(testTopicId);
        request.setProducerId(testPublishId);

        OnsPublishCreateResponse response = iAcsClient.getAcsResponse(request);
        System.out.printf("[AliyunMq TestProducer] %s is created for %s!\nRequestId=%s, HelpUrl=%s\n",
                testPublishId, testTopicId, response.getRequestId(), response.getHelpUrl());
        //EE: wait for initialization
        if (isPublicTest()) {
            out.println("sleep 30 seconds to wait for initialization");
            Thread.sleep(30 * Second);
        } else {
            out.println("sleep 3 seconds to wait for initialization");
            Thread.sleep(3 * Second);
        }

        return true;
    }

    protected boolean createConsumerSubscription(String testTopicId, String testConsumerId) throws ClientException, InterruptedException {

        OnsSubscriptionCreateRequest request = new OnsSubscriptionCreateRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(testTopicId);
        request.setConsumerId(testConsumerId);

        OnsSubscriptionCreateResponse response = iAcsClient.getAcsResponse(request);
        System.out.printf("[AliyunMq TestConsumer] %s is created for %s!\n", testConsumerId, testTopicId);
        //EE: wait for initialization
        if (isPublicTest()) {
            out.println("sleep 15 seconds to wait for initialization");
            Thread.sleep(15 * Second);
        } else {
            out.println("sleep 3 seconds to wait for initialization");
            Thread.sleep(3 * Second);
        }

        return true;
    }

    protected boolean deleteConsumerSubscription(String testTopicId, String testConsumerId) {

        OnsSubscriptionDeleteRequest request = new OnsSubscriptionDeleteRequest();
        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
//        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(testTopicId);
        request.setConsumerId(testConsumerId);

        OnsSubscriptionDeleteResponse response = null;
        try {
            response = iAcsClient.getAcsResponse(request);
            System.out.printf("[AliyunMq TestConsumer] %s is created for %s!\nRequestId=%s, HelpUrl=%s\n",
                    testConsumerId, testTopicId, response.getRequestId(), response.getHelpUrl());
        } catch (ClientException e) {
            System.out.printf("[AliyunMq TestConsumer] %s fail to be deleted %s!\n",
                    testConsumerId, testTopicId);
            out.println(e.getErrCode());
            out.println(e.getErrMsg());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean deleteProducerPublish(String testTopicId, String testProducerId) {

        OnsPublishDeleteRequest request = new OnsPublishDeleteRequest();

        request.setOnsRegionId(ONS_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic(testTopicId);
        request.setProducerId(testProducerId);

        OnsPublishDeleteResponse response = null;
        try {
            response = iAcsClient.getAcsResponse(request);
            System.out.printf("[AliyunMq TestProducer] %s is deleted for %s!\nRequestId=%s, HelpUrl=%s\n",
                    testProducerId, testTopicId, response.getRequestId(), response.getHelpUrl());
        } catch (ClientException e) {
            System.out.printf("[AliyunMq TestProducer] %s fail to be deleted %s!\n",
                    testProducerId, testTopicId);
            e.printStackTrace();
        }
        return true;
    }

    protected Producer createProducer(String testProducerId) throws IllegalAccessException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ProducerId, testProducerId);
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);

        if (isPublicTest()) {
            properties.put(PropertyKeyConst.SendMsgTimeoutMillis, 15 * Second);
        } else {
            properties.put(PropertyKeyConst.ONSAddr, ONS_ADDRESS);
        }

        Producer producer = ONSFactory.createProducer(properties);
        producer.start();
        out.printf("ONS Producer is started!%s\n", producer.getClass());
        return producer;
    }


    protected Consumer createDefaultConsumer(String testTopicId, String testConsumerId) {
        return createDefaultConsumer(testTopicId, testConsumerId, 16);
    }

    protected Consumer createDefaultConsumer(String testTopicId, String testConsumerId, int maxReconsumeTimes) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ConsumerId, testConsumerId);
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        properties.put(PropertyKeyConst.ConsumeThreadNums, 1);
        properties.put(PropertyKeyConst.MaxReconsumeTimes, maxReconsumeTimes);
        if (isPublicTest()) {
            //TODO
        } else {
            properties.put(PropertyKeyConst.ONSAddr, ONS_ADDRESS);
        }

        Consumer consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe(testTopicId, "*", new ResumeMessageListener());
//        consumer.start();
        return consumer;
    }

    protected void printAllTopics() {

    }

}
