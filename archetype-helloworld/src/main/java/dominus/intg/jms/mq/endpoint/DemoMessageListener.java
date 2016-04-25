package dominus.intg.jms.mq.endpoint;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

import java.util.concurrent.atomic.AtomicLong;


public class DemoMessageListener implements MessageListener {

    public static AtomicLong count = new AtomicLong(0L);
    Long sleepInterval;

    public DemoMessageListener() {
    }

    public DemoMessageListener(Long sleepInterval) {
        this.sleepInterval = sleepInterval;
    }

    public Action consume(Message message, ConsumeContext context) {
        System.out.printf("DemoMessageListener Receive: %s [%s]\n", message.getKey(), message.getMsgID());
        count.incrementAndGet();
        if (sleepInterval != 0)
            try {
                System.out.println("doing business process......");
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return Action.CommitMessage;
    }
}
