package dominus.bigdata.connector.sqoop;


import dominus.junit.DominusBaseTestCase;
import org.apache.sqoop.client.SqoopClient;
import org.apache.sqoop.client.SubmissionCallback;
import org.apache.sqoop.common.SqoopException;
import org.apache.sqoop.model.*;
import org.apache.sqoop.validation.Status;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Prerequisite:
 * <p/>
 * EE: Sqoop Server / mysql employee table / hdfs
 * show connector
 * +----+------------------------+-----------------+------------------------------------------------------+----------------------+
 * | Id |          Name          |     Version     |                        Class                         | Supported Directions |
 * +----+------------------------+-----------------+------------------------------------------------------+----------------------+
 * | 1  | generic-jdbc-connector | 1.99.5-cdh5.5.2 | org.apache.sqoop.connector.jdbc.GenericJdbcConnector | FROM/TO              |
 * | 2  | kite-connector         | 1.99.5-cdh5.5.2 | org.apache.sqoop.connector.kite.KiteConnector        | FROM/TO              |
 * | 3  | hdfs-connector         | 1.99.5-cdh5.5.2 | org.apache.sqoop.connector.hdfs.HdfsConnector        | FROM/TO              |
 * | 4  | kafka-connector        | 1.99.5-cdh5.5.2 | org.apache.sqoop.connector.kafka.KafkaConnector      | TO                   |
 * +----+------------------------+-----------------+------------------------------------------------------+----------------------+
 * <p/>
 * <p/>
 * EE: Link lifecycle(one time, job finished and data source links should be deleted)
 * EE: Central security/credentials store
 */
public class SqoopConnectorTest extends DominusBaseTestCase {

    SqoopClient client;
    MConnector jdbcConnector;
    MConnector hdfsConnector;
    //one time job
    MLink mysqlFromLink;
    MLink hdfsToLink;
    MJob mysql2HdfsJob;

    /**
     * EE: Setup topology information(connector & link), password
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            client = new SqoopClient(properties.getProperty("sqoop.server.url"));
            Collection<MConnector> connectors = client.getConnectors();
            assertTrue(connectors.size() > 0);
            for (MConnector connector : connectors) {
                out.println(connector.toString());
                if (connector.getUniqueName().contains("jdbc"))
                    jdbcConnector = connector;
                if (connector.getUniqueName().contains("hdfs"))
                    hdfsConnector = connector;
            }
            assertTrue("jdbcConnector or hdfsConnector does not existed in sqoop server!", (jdbcConnector != null && hdfsConnector != null));

            //EE: create mysql jdbc link
            mysqlFromLink = client.createLink(jdbcConnector.getPersistenceId());
            mysqlFromLink.setName("MysqlFromLink");
            mysqlFromLink.setCreationUser("shawguo");
            MLinkConfig linkConfig = mysqlFromLink.getConnectorLinkConfig();

            // fill in the link config values
            linkConfig.getStringInput("linkConfig.connectionString").setValue(properties.getProperty("scala.mysql.jdbc.url"));
            linkConfig.getStringInput("linkConfig.jdbcDriver").setValue("com.mysql.jdbc.Driver");
            linkConfig.getStringInput("linkConfig.username").setValue(properties.getProperty("scala.mysql.jdbc.username"));
            linkConfig.getStringInput("linkConfig.password").setValue(properties.getProperty("scala.mysql.jdbc.password"));
            // save the link object that was filled
            Status status = client.saveLink(mysqlFromLink);
            assertTrue("Something went wrong creating MyQL link", status.canProceed());
            out.printf("Sqoop link [%s, %s] is created..\n", mysqlFromLink.getName(), mysqlFromLink.getPersistenceId());

            //EE: create hdfs link
            hdfsToLink = client.createLink(hdfsConnector.getPersistenceId());
            hdfsToLink.setName("HdfsToLink");
            hdfsToLink.setCreationUser("shawguo");
            linkConfig = hdfsToLink.getConnectorLinkConfig();
            linkConfig.getStringInput("linkConfig.uri").setValue(properties.getProperty("sqoop.hdfs.uri"));
            status = client.saveLink(hdfsToLink);
            assertTrue("Something went wrong creating HDFS link", status.canProceed());
            out.printf("Sqoop link [%s, %s] is created..\n", hdfsToLink.getName(), hdfsToLink.getPersistenceId());

        } catch (SqoopException exception) {
            exception.printStackTrace();
            //cleanup new created links
            if (mysqlFromLink != null && mysqlFromLink.hasPersistenceId())
                client.deleteLink(mysqlFromLink.getPersistenceId());
            if (hdfsToLink != null && hdfsToLink.hasPersistenceId())
                client.deleteLink(hdfsToLink.getPersistenceId());
        }
    }

    public void testMysqlToHdfsConnector() throws InterruptedException {

        //EE: create Sqoop job
        mysql2HdfsJob = client.createJob(mysqlFromLink.getPersistenceId(), hdfsToLink.getPersistenceId());
        mysql2HdfsJob.setName("Job-Mysql2Hdfs");
        mysql2HdfsJob.setCreationUser("shawguo");
        MFromConfig fromConfig = mysql2HdfsJob.getFromJobConfig();
        MToConfig toConfig = mysql2HdfsJob.getToJobConfig();

        //mysql from link config
        fromConfig.getStringInput("fromJobConfig.schemaName").setValue(properties.getProperty("scala.mysql.jdbc.schema"));
        fromConfig.getStringInput("fromJobConfig.tableName").setValue(properties.getProperty("scala.mysql.jdbc.table"));
        fromConfig.getStringInput("fromJobConfig.partitionColumn").setValue("emp_no");

        //set the HDFS "TO" link job config values
        toConfig.getEnumInput("toJobConfig.outputFormat").setValue("TEXT_FILE");
        toConfig.getEnumInput("toJobConfig.compression").setValue("NONE");
        toConfig.getStringInput("toJobConfig.outputDirectory").setValue("/user/shawguo/sqoop_output");
        // set the driver config values
        MDriverConfig driverConfig = mysql2HdfsJob.getDriverConfig();
        driverConfig.getIntegerInput("throttlingConfig.numExtractors").setValue(3);

        Status status = client.saveJob(mysql2HdfsJob);
        if (status.canProceed()) {
            System.out.println("Created Job with Job Id: " + mysql2HdfsJob.getPersistenceId());
        } else {
            System.out.println("Something went wrong creating the job");
        }
        assertTrue(mysql2HdfsJob.hasPersistenceId());

        //EE: start Sqoop Job
        MSubmission submission = client.startJob(mysql2HdfsJob.getPersistenceId(), new DefaultSubmissionCallback(), 1000);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mysql2HdfsJob != null && mysql2HdfsJob.hasPersistenceId()) {
            client.deleteJob(mysql2HdfsJob.getPersistenceId());
            System.out.printf("Sqoop job [%s, %s] is deleted..\n", mysql2HdfsJob.getName(), mysql2HdfsJob.getPersistenceId());
        }
        if (mysqlFromLink != null && mysqlFromLink.hasPersistenceId()) {
            client.deleteLink(mysqlFromLink.getPersistenceId());
            System.out.printf("Sqoop link [%s, %s] is deleted..\n", mysqlFromLink.getName(), mysqlFromLink.getPersistenceId());
        }
        if (hdfsToLink != null && hdfsToLink.hasPersistenceId()) {
            client.deleteLink(hdfsToLink.getPersistenceId());
            System.out.printf("Sqoop link [%s, %s] is deleted..\n", hdfsToLink.getName(), hdfsToLink.getPersistenceId());
        }
    }

    public void testPrintConnectorDetail() {
        Collection<MConnector> connectors = client.getConnectors();
        assertTrue(connectors.size() > 0);
        for (MConnector connector : connectors) {
            long connectorId = connector.getPersistenceId();
            out.println("[Connector]: " + connector.getUniqueName());
            // link config for connector
            describe(client.getConnector(connectorId).getLinkConfig().getConfigs(), client.getConnectorConfigBundle(connectorId));
            // from job config for connector
            if (client.getConnector(connectorId).getFromConfig() != null)
                describe(client.getConnector(connectorId).getFromConfig().getConfigs(), client.getConnectorConfigBundle(connectorId));
            // to job config for the connector
            if (client.getConnector(connectorId).getToConfig() != null)
                describe(client.getConnector(connectorId).getToConfig().getConfigs(), client.getConnectorConfigBundle(connectorId));
            out.print("\n\n\n");
        }
    }

    void describe(List<MConfig> configs, ResourceBundle resource) {
        for (MConfig config : configs) {
            System.out.println(resource.getString(config.getLabelKey()) + ":");
            List<MInput<?>> inputs = config.getInputs();
            for (MInput input : inputs) {
                System.out.printf("[Name]: %s \n\t%s\n", input.getName(),
                        resource.getString(input.getHelpKey()));
            }
        }
    }

    static class DefaultSubmissionCallback implements SubmissionCallback {
        @Override
        public void submitted(MSubmission submission) {
            System.out.println(submission);
        }

        @Override
        public void updated(MSubmission submission) {
            System.out.println(submission);
        }

        @Override
        public void finished(MSubmission submission) {
            System.out.println(submission);
        }
    }


}
