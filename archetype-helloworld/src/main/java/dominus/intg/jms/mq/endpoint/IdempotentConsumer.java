package dominus.intg.jms.mq.endpoint;


import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

public interface IdempotentConsumer {

    abstract public boolean isDuplicate(String md5Key);
}
