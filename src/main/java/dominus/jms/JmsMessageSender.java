package dominus.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Scheduled message sender
 */
@Component(value = "messageSender")
public class JmsMessageSender {

    private static final Log logger = LogFactory.getLog(JmsMessageSender.class);

    @Autowired
    JmsTemplate jmsTemplate;

    /**
     * Notice that the methods to be scheduled must have void returns and must not expect any arguments.
     */
    @Scheduled(fixedRate = 2000)
    public void sendMsg_1() {
        jmsTemplate.send(JmsConstant.DESTINATION, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage msg = session.createTextMessage("ping!");
                logger.info(Thread.currentThread().getName() + " / sending text message [1]");
                return msg;
            }
        });
    }

    @Scheduled(fixedRate = 1000, initialDelay = 0)
    public void sendMsg_2() {
        jmsTemplate.send(JmsConstant.DESTINATION, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage msg = session.createTextMessage("ping!");
                logger.info(Thread.currentThread().getName() + " / sending text message [2]");
                return msg;
            }
        });
    }

}
