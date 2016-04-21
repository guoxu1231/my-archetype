package dominus.intg.jms.mq;


import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.ons.model.v20160405.*;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

import java.util.List;

/**
 * 注意：因为POP网关是面对公网环境提供服务的，因此使用Open-API的前提是，客户端需要能够访问公网服务。否则会提示服务无法连接
 */
public class TestAliyunMqAdmin extends DominusJUnit4TestBase {


    final String ACS_REGION_ID = "cn-hangzhou";
    final String OSN_REGION_ID = "cn-hangzhou";
    String accessKey;
    String secretKey;

    @Override
    protected void doSetUp() throws Exception {
        accessKey = properties.getProperty("aliyun.accessKey");
        secretKey = properties.getProperty("aliyun.secretKey");
    }

    @Override
    protected void doTearDown() throws Exception {
    }


    /**
     * 1  cn-qingdao-publictest  公网测试  null    1411623866000  1411623866000
     * 3  cn-qingdao  华北 1 (青岛)  null    1411623866000  1411623866000
     * 4  cn-shenzhen  华南 1 (深圳)  null    1411623866000  1411623866000
     * 6  cn-hangzhou  华东 1 (杭州)  null    1413877180000  1413877180000
     * 8  cn-beijing  华北 2 (北京)  null    1413008259000  1413008259000
     * 9  cn-shanghai  华东 2 (上海)  null    1452840680000  1452840688000
     *
     * @throws ClientException
     */
    @Test
    public void testOnsRegionListRequest() throws ClientException {
        IClientProfile profile = DefaultProfile.getProfile(ACS_REGION_ID, accessKey, secretKey);
        IAcsClient client = new DefaultAcsClient(profile);
        OnsRegionListRequest request = new OnsRegionListRequest();
        request.setAcceptFormat(FormatType.JSON);
        request.setPreventCache(System.currentTimeMillis());

        OnsRegionListResponse response = client.getAcsResponse(request);
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
    public void testListTopic() throws ClientException {
        IClientProfile profile = DefaultProfile.getProfile(ACS_REGION_ID, accessKey, secretKey);
        IAcsClient client = new DefaultAcsClient(profile);

        OnsTopicListRequest request = new OnsTopicListRequest();
        request.setOnsRegionId(OSN_REGION_ID);
        request.setPreventCache(System.currentTimeMillis());

        OnsTopicListResponse response = client.getAcsResponse(request);
        List<OnsTopicListResponse.PublishInfoDo> publishInfoDoList = response.getData();
        for (OnsTopicListResponse.PublishInfoDo publishInfoDo : publishInfoDoList) {
            System.out.println(publishInfoDo.getTopic() + "     " + publishInfoDo.getOwner());
        }

    }

    @Test
    public void testOnsTopicCreateRequest() throws ClientException {
        IClientProfile profile = DefaultProfile.getProfile(ACS_REGION_ID, accessKey, secretKey);
        IAcsClient client = new DefaultAcsClient(profile);

        OnsTopicCreateRequest request = new OnsTopicCreateRequest();
        request.setAcceptFormat(FormatType.JSON);
        request.setTopic("D-GUOXU-TEST-160418");
        request.setQps(1000l);
        request.setRemark("DEV");
        request.setStatus(0);
        /**
         *ONSRegionId是指你需要API访问ONS哪个区域的资源。
         *该值必须要根据OnsRegionList方法获取的列表来选择和配置，因为OnsRegionId是变动的，不能够写固定值
         */
        request.setOnsRegionId(OSN_REGION_ID);
        request.setCluster("DEV-Test");
        request.setPreventCache(System.currentTimeMillis());

        OnsTopicCreateResponse response = client.getAcsResponse(request);
        System.out.println(response.getRequestId());
    }

    @Test
    public void testOnsTopicDeleteRequest() throws ClientException {

        IClientProfile profile = DefaultProfile.getProfile(ACS_REGION_ID, accessKey, secretKey);
        IAcsClient client = new DefaultAcsClient(profile);

        OnsTopicDeleteRequest request = new OnsTopicDeleteRequest();
        request.setPreventCache(System.currentTimeMillis());
        request.setOnsRegionId(OSN_REGION_ID);
        request.setTopic("D-GUOXU-TEST-160418");

        OnsTopicDeleteResponse response = client.getAcsResponse(request);
        System.out.println(response.getRequestId());

    }


}
