package dominus.intg.mongodb;


import dominus.junit.DominusBaseTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;


/**
 * mongodb/bin/mongod --dbpath /opt/Development/middleware/mongodb/data
 */
public class MongoIntgTest extends DominusBaseTestCase {

    ApplicationContext context;
    MongoOperations mongoOps;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = new ClassPathXmlApplicationContext(new String[]{"mongodb_context.xml"});
        mongoOps = context.getBean(MongoTemplate.class);
        assertTrue(mongoOps.collectionExists("iops_schema"));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testSaveOperation() {
        Employee employee = new Employee(21, new Date(), "shawn", "guo", "M", new Date());
        mongoOps.save(employee);
        //TODO assert
    }

}
