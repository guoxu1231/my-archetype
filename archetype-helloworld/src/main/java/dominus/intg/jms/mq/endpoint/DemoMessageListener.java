package dominus.intg.jms.mq.endpoint;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

import java.util.concurrent.atomic.AtomicLong;


public class DemoMessageListener implements MessageListener {

    public static AtomicLong count = new AtomicLong(0L);

    public Action consume(Message message, ConsumeContext context) {
        System.out.printf("DemoMessageListener Receive: %s [%s]\n", message.getKey(), message.getMsgID());
        count.incrementAndGet();
        return Action.CommitMessage;
    }
}
