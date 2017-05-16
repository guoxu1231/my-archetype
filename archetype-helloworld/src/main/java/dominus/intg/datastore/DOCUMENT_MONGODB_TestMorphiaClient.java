package dominus.intg.datastore;


import com.mongodb.MongoClient;
import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.intg.datastore.mongodb.Employee;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import org.mongodb.morphia.logging.slf4j.SLF4JLoggerImplFactory;
import org.mongodb.morphia.query.Query;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DOCUMENT_MONGODB_TestMorphiaClient extends DominusJUnit4TestBase {

    Datastore datastore;

    static {
        MorphiaLoggerFactory.registerLogger(SLF4JLoggerImplFactory.class);
    }

    @Override
    protected void doSetUp() throws Exception {
        java.util.logging.Logger.getLogger("com.mongodb.TRACE").setLevel(java.util.logging.Level.INFO);
        super.doSetUp();
        final Morphia morphia = new Morphia();

        // tell Morphia where to find your classes can be called multiple times with different packages or classes
        morphia.mapPackage("dominus.intg.datastore.mongodb");

        datastore = morphia.createDatastore(new MongoClient(), "morphia_example");
        datastore.ensureIndexes();
        Employee employeeOne = new Employee(21086, new Date(), "shawn", "guo", "M", new Date());
        employeeOne.addFlexField("bugid", "shawguo");
        employeeOne.addFlexField("vpn", "shawguo_cn");
        datastore.save(employeeOne);
        Employee employeeTwo = new Employee(30030, new Date(), "Steve", "Jobs", "M", new Date());
        employeeTwo.addFlexField("bugid", "jobs");
        employeeTwo.addFlexField("vpn", "Jobs_cn");
        datastore.save(employeeTwo);
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        datastore.delete(datastore.createQuery(Employee.class));
    }

    @Test
    public void testCRUD() {
        final Employee elmer = new Employee(21, new Date(), "shawn", "guo", "M", new Date());
        datastore.save(elmer);
    }

    @Test
    public void testQueryByFieldInArray() {
        Employee emp = datastore.createQuery(Employee.class).filter("emp_no =", 21086).get();
        assertEquals("shawguo_cn", emp.getFlexField("vpn"));

        Query q = datastore.createQuery(Employee.class);
        q.field("flex_fields.field_value").equal("jobs");
        Employee jobs = (Employee) q.get();
        assertTrue(jobs != null);
        System.out.println(jobs);

        jobs = datastore.createQuery(Employee.class).filter("flex_fields.field_value =", "jobs").get();
        assertTrue(jobs != null);
        System.out.println(jobs);

        //EE:smart translate from java field name to stored field name
        q = datastore.createQuery(Employee.class);
        q.field("flexFields.fieldValue").equal("shawguo");
        jobs = (Employee) q.get();
        assertTrue(jobs != null);
        System.out.println(jobs);

        jobs = datastore.createQuery(Employee.class).filter("flexFields.fieldValue =", "shawguo").get();
        assertTrue(jobs != null);
        System.out.println(jobs);
    }


}
