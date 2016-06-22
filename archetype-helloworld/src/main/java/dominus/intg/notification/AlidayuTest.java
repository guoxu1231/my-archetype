package dominus.intg.notification;


import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.request.AlibabaAliqinFcTtsNumSinglecallRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;
import com.taobao.api.response.AlibabaAliqinFcTtsNumSinglecallResponse;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import static org.junit.Assert.*;
//import com.

import java.io.IOException;
import java.util.Arrays;

public class AlidayuTest extends DominusJUnit4TestBase {
    String appKey;
    String appSecret;
    String templateCode;
    String ttsCode;
    String smsSign;
    String recNum;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        appKey = properties.getProperty("alidayu.appkey");
        appSecret = properties.getProperty("alidayu.appsecret");
        templateCode = properties.getProperty("alidayu.template");
        smsSign = properties.getProperty("alidayu.sms.sign");
        recNum = properties.getProperty("alidayu.rec_num");
        ttsCode = properties.getProperty("alidayu.ttsCode");
    }

    //EE: aliyun dayu
    @Test
    public void testRESTSendSms() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //	请求地址
        HttpUriRequest httpGet = RequestBuilder
                .get("https://ca.aliyuncs.com/gw/alidayu/sendSms")
                .addHeader("X-Ca-Key", appKey)
                .addHeader("X-Ca-Secret", appSecret)
                .addParameter("rec_num", recNum)
                .addParameter("sms_template_code", templateCode)
                .addParameter("sms_free_sign_name", smsSign)
                .addParameter("sms_type", "normal")
                .addParameter("extend", "1234")
                .addParameter("sms_param", "{'content':'hello world'}")
                .build();
        //	TODO	设置请求超时时间
        //	处理请求结果
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                System.out.println(status);
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            }
        };
        //	发起 API 调用
        String responseBody = httpClient.execute(httpGet, responseHandler);
        System.out.println(responseBody);
        httpClient.close();
    }

    //EE: alidayu
    @Test
    public void testSDKSendSMS() throws ApiException {

        TaobaoClient client = new DefaultTaobaoClient("https://eco.taobao.com/router/rest", appKey, appSecret);
        AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
        req.setExtend("123456");
        req.setSmsType("normal");
        req.setSmsFreeSignName(smsSign);
        req.setSmsParamString("{\"content\":\"hello world\"}");
        req.setRecNum(recNum);
        req.setSmsTemplateCode(templateCode);
        out.println(Arrays.toString(req.getTextParams().entrySet().toArray()));
        AlibabaAliqinFcSmsNumSendResponse rsp = client.execute(req);
        System.out.println(rsp.getBody());
        assertTrue(rsp.isSuccess());
    }

    @Test
    public void testSingleCall() throws ApiException {
        TaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest", appKey, appSecret);
        AlibabaAliqinFcTtsNumSinglecallRequest req = new AlibabaAliqinFcTtsNumSinglecallRequest();
        req.setExtend("12345");
        req.setTtsParamString("{\"content\":\",hello world\"}");
        req.setCalledNum(recNum);
        req.setCalledShowNum("051482043270");
        req.setTtsCode(ttsCode);
        AlibabaAliqinFcTtsNumSinglecallResponse rsp = client.execute(req);
        System.out.println(rsp.getBody());
    }


}
