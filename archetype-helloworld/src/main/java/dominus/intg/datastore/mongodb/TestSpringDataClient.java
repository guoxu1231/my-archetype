package dominus.intg.datastore.mongodb;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoAdmin;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;


/**
 * EE: Local
 * mongodb/bin/mongod --dbpath /opt/Development/middleware/mongodb/data
 */
@ContextConfiguration(locations = {"classpath:spring-container/mongodb_context.xml"})
public class TestSpringDataClient extends DominusJUnit4TestBase {

    static String TEST_SCHEMA = "test_schema";

    @Autowired
    MongoOperations mongoOps;

    @Autowired
    MongoAdmin admin;

    @Override
    protected void doSetUp() throws Exception {
        admin.setAuthenticationDatabaseName("admin");
        admin.createDatabase(TEST_SCHEMA);
        out.println(admin.getDatabaseStats(TEST_SCHEMA));
    }

    @Override
    protected void doTearDown() throws Exception {
        out.println(admin.getDatabaseStats(TEST_SCHEMA));
        admin.dropDatabase(TEST_SCHEMA);
        out.println(TEST_SCHEMA + " is deleted!");
    }

    @Test
    public void testCRUD() {
        Employee employee = new Employee(21, new Date(), "shawn", "guo", "M", new Date());
        mongoOps.save(employee);
        List<Employee> employees = mongoOps.find(new Query(Criteria.where("emp_no").is(21)), Employee.class);
        assertTrue(employees.size() == 1);
        assertEquals(employee,employees.get(0));
    }

}
