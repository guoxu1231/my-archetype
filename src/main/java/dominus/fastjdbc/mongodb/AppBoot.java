package dominus.fastjdbc.mongodb;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;

public class AppBoot {

    public static void main(String[] args) {

        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"mongodb_context.xml"});

        MongoOperations mongoOps = context.getBean(MongoTemplate.class);
        Employee employee = new Employee(21,new Date(),"shawn", "guo", "M", new Date());
        mongoOps.save(employee);
    }


}
