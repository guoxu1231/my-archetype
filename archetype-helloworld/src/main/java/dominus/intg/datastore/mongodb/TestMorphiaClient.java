package dominus.intg.datastore.mongodb;


import com.mongodb.MongoClient;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.Date;

public class TestMorphiaClient extends DominusJUnit4TestBase {

    Datastore datastore;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        final Morphia morphia = new Morphia();

        // tell Morphia where to find your classes can be called multiple times with different packages or classes
        morphia.mapPackage("dominus.intg.datastore.mongodb");

        datastore = morphia.createDatastore(new MongoClient(), "morphia_example");
        datastore.ensureIndexes();
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }

    @Test
    public void testCRUD() {
        final Employee elmer = new Employee(21, new Date(), "shawn", "guo", "M", new Date());
        datastore.save(elmer);
    }


}
