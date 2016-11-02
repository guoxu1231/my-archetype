package dominus.intg.message.activemq;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppBoot {

    public static void main(String[] args) {

        // Launch the application
        System.out.println("Hello world");
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"jms_context.xml"});

    }
}