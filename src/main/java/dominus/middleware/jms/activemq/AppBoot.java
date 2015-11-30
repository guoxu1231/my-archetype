package dominus.middleware.jms.activemq;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

public class AppBoot {

    public static void main(String[] args) {

        // Launch the application
        System.out.println("Hello world");
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"jms_context.xml"});

    }
}