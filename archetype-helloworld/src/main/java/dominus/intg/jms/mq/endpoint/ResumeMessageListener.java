package dominus.intg.jms.mq.endpoint;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

import java.util.concurrent.atomic.AtomicLong;


public class ResumeMessageListener implements MessageListener {

    public Action consume(Message message, ConsumeContext context) {
        System.out.printf("DemoMessageListener Receive: %s [%s] [ReconsumeTimes] %d\n", message.getKey(), message.getMsgID(), message.getReconsumeTimes());

        return Action.ReconsumeLater;
    }
}
