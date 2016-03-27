package dominus.intg.jms.activemq;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import javax.jms.Message;
import javax.jms.MessageListener;


/**
 * One of the most common uses of JMS messages in the EJB world is to drive message-driven beans (MDBs).
 * Spring offers a solution to create message-driven POJOs (MDPs) in a way that does not tie a user to an EJB container.
 */
@Component(value = "messageListener_1")
public class JmsMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        System.out.println("Received <" + message + ">");
    }
}