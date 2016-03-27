package dominus.bigdata.workflow;


import dominus.framework.junit.DominusBaseTestCase;
import dominus.framework.junit.annotation.HdfsClient;
import dominus.language.network.SSLUtilities;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;

import java.util.Arrays;
import java.util.Properties;

@HdfsClient
public class OozieActionTest extends DominusBaseTestCase {

    OozieClient client;
    Properties commonProps;

    public OozieActionTest() {
        super();
    }

    public OozieActionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //unable to find valid certification path to requested target
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
//      System.setProperty("javax.net.ssl.trustStore", "cert/scaj31bda04.us.oracle.com");
        System.setProperty("javax.net.debug", "all");
        client = new OozieClient(properties.getProperty("oozie.url"));
        client.validateWSVersion();
        out.printf("AvailableOozieServers:%s\n", Arrays.toString(client.getAvailableOozieServers().entrySet().toArray()));
        assertTrue(client.getAvailableOozieServers().size() >= 1);

        //setup common config
        commonProps = client.createConfiguration();
        commonProps.setProperty("jobTracker", properties.getProperty("oozie.conf.job-tracker"));
        commonProps.setProperty("nameNode", properties.getProperty("oozie.conf.name-node"));
        commonProps.setProperty("queueName", "default");
        commonProps.setProperty(OozieClient.USER_NAME, "oracle");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testJavaAction() throws OozieClientException, InterruptedException {
        // create a workflow job configuration and set the workflow application path
        Properties conf = client.createConfiguration();
        conf.putAll(commonProps);
        conf.setProperty(OozieClient.APP_PATH, "hdfs://scaj31bda02.us.oracle.com:8020/user/shawguo/oozie/examples/apps/java-main");
        String jobId = submitOozieJob(conf);
        assertEquals(client.getJobInfo(jobId).getStatus(), WorkflowJob.Status.SUCCEEDED);
    }

    public void testShellAction() throws OozieClientException, InterruptedException {
        Properties conf = client.createConfiguration();
        conf.putAll(commonProps);
        conf.setProperty(OozieClient.APP_PATH, "hdfs://scaj31bda02.us.oracle.com:8020/user/shawguo/oozie/examples/apps/shell");
        String jobId = submitOozieJob(conf);
        assertEquals(client.getJobInfo(jobId).getStatus(), WorkflowJob.Status.SUCCEEDED);
    }

    //submit and start the workflow job
    String submitOozieJob(Properties conf) throws OozieClientException, InterruptedException {
        String jobId = null;

        jobId = client.run(conf);
        System.out.println("Workflow job submitted");
        // wait until the workflow job finishes printing the status every 10 seconds
        while (client.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
            System.out.println("Workflow job running ...");
            Thread.sleep(10 * 1000);
        }
        // print the final status of the workflow job
        System.out.println("Workflow job completed ...");
        System.out.println(client.getJobInfo(jobId));
        return jobId;
    }

}
