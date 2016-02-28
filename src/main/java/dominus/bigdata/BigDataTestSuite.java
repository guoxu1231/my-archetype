package dominus.bigdata;


import dominus.bigdata.connector.sqoop.SqoopConnectorTest;
import dominus.bigdata.workflow.OozieActionTest;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//EE: Aggregating tests in suites
@RunWith(Suite.class)
@Suite.SuiteClasses({OozieActionTest.class, SqoopConnectorTest.class})
public class BigDataTestSuite extends TestSuite {

    /**JUnit 3.8.x static Test suite() method.
     public static Test suite() {
     TestSuite suite = new TestSuite();
     suite.addTest(new OozieActionTest());
     return suite;
     }
     **/
}
