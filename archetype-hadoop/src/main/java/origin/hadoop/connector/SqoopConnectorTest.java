package origin.hadoop.connector;


import org.apache.sqoop.client.SqoopClient;
import org.apache.sqoop.client.SubmissionCallback;
import org.apache.sqoop.common.SqoopException;
import org.apache.sqoop.model.*;
import org.apache.sqoop.submission.SubmissionStatus;
import org.apache.sqoop.validation.Status;
import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import static junit.framework.TestCase.assertTrue;


/**
 * Prerequisite:
 * <p>
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
 * <p>
 * <p>
 * EE: Link lifecycle(one time, job finished and data source links should be deleted)
 * EE: Central security/credentials store
 */
public class SqoopConnectorTest extends DominusJUnit4TestBase {

    SqoopClient client;
    MConnector jdbcConnector;
    MConnector hdfsConnector;
    MConnector kafkaConnector;
    //shared link config
    MLink mysqlFromLink = null;
    MLink hdfsToLink = null;
    MLink kafkaToLink = null;
    //one time job
    MJob mysql2HdfsJob;
    MJob mysql2KafkaJob;

    /**
     * EE: Setup topology information(connector & link), password
     *
     * @throws Exception
     */
    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        try {
            client = new SqoopClient(properties.getProperty("sqoop.server.url"));
            Collection<MConnector> connectors = client.getConnectors();
            assertTrue(connectors.size() > 0);
            for (MConnector connector : connectors) {
                logger.info(connector.toString());
                if (connector.getUniqueName().contains("jdbc"))
                    jdbcConnector = connector;
                if (connector.getUniqueName().contains("hdfs"))
                    hdfsConnector = connector;
                if (connector.getUniqueName().contains("kafka"))
                    kafkaConnector = connector;
            }

            //EE: create mysql jdbc link
            mysqlFromLink = client.createLink(jdbcConnector.getPersistenceId());
            mysqlFromLink.setName("MysqlFromLink-" + System.currentTimeMillis());
            mysqlFromLink.setCreationUser("shawguo");
            MLinkConfig linkConfig = mysqlFromLink.getConnectorLinkConfig();
            // fill in the link config values
            linkConfig.getStringInput("linkConfig.connectionString").setValue(properties.getProperty("sqoop.mysql.jdbc.url"));
            linkConfig.getStringInput("linkConfig.jdbcDriver").setValue("com.mysql.jdbc.Driver");
            linkConfig.getStringInput("linkConfig.username").setValue(properties.getProperty("sqoop.mysql.jdbc.username"));
            linkConfig.getStringInput("linkConfig.password").setValue(properties.getProperty("sqoop.mysql.jdbc.password"));
            // save the link object that was filled, maybe failed caused by missing drivers jar.
            Status status = client.saveLink(mysqlFromLink);
            assertTrue("Something went wrong creating MyQL link", status.canProceed());
            logger.info("Sqoop link [{}, {}] is created..", mysqlFromLink.getName(), mysqlFromLink.getPersistenceId());

            //EE: create hdfs link
            hdfsToLink = client.createLink(hdfsConnector.getPersistenceId());
            hdfsToLink.setName("HdfsToLink-" + System.currentTimeMillis());
            hdfsToLink.setCreationUser(properties.getProperty("hadoop.user"));
            linkConfig = hdfsToLink.getConnectorLinkConfig();
            linkConfig.getStringInput("linkConfig.uri").setValue(properties.getProperty("sqoop.hdfs.uri"));
            status = client.saveLink(hdfsToLink);
            assertTrue("Something went wrong creating HDFS link", status.canProceed());
            logger.info("Sqoop link [{}, {}] is created..", hdfsToLink.getName(), hdfsToLink.getPersistenceId());

            //EE: create kafka link
            kafkaToLink = client.createLink(kafkaConnector.getPersistenceId());
            kafkaToLink.setName("KafkaToLink-" + System.currentTimeMillis());
            MLinkConfig kafkaLinkConfig = kafkaToLink.getConnectorLinkConfig();
            kafkaLinkConfig.getStringInput("linkConfig.brokerList").setValue(properties.getProperty("zkQuorum"));
            kafkaLinkConfig.getStringInput("linkConfig.zookeeperConnect").setValue(properties.getProperty("bootstrap.servers"));
            status = client.saveLink(kafkaToLink);
            assertTrue("Something went wrong creating Kafka link", status.canProceed());
            logger.info("Sqoop link [{}, {}] is created..", kafkaToLink.getName(), kafkaToLink.getPersistenceId());

        } catch (SqoopException exception) {
            exception.printStackTrace();
            //cleanup new created links
            if (mysqlFromLink != null && mysqlFromLink.hasPersistenceId())
                client.deleteLink(mysqlFromLink.getPersistenceId());
            if (hdfsToLink != null && hdfsToLink.hasPersistenceId())
                client.deleteLink(hdfsToLink.getPersistenceId());
        }
    }


    @Test
    public void testMysqlToHdfs() throws InterruptedException {
        //EE: create Sqoop job
        mysql2HdfsJob = client.createJob(mysqlFromLink.getPersistenceId(), hdfsToLink.getPersistenceId());
        mysql2HdfsJob.setName("Job-Mysql2Hdfs-" + System.currentTimeMillis());
        mysql2HdfsJob.setCreationUser(properties.getProperty("hadoop.user"));
        MFromConfig fromConfig = mysql2HdfsJob.getFromJobConfig();
        MToConfig toConfig = mysql2HdfsJob.getToJobConfig();

        //mysql from link config
        fromConfig.getStringInput("fromJobConfig.schemaName").setValue(properties.getProperty("sqoop.mysql.jdbc.schema"));
        fromConfig.getStringInput("fromJobConfig.tableName").setValue(properties.getProperty("sqoop.mysql.jdbc.table"));
        fromConfig.getStringInput("fromJobConfig.partitionColumn").setValue("emp_no");

        //set the HDFS "TO" link job config values
        toConfig.getEnumInput("toJobConfig.outputFormat").setValue("TEXT_FILE");
        toConfig.getEnumInput("toJobConfig.compression").setValue("NONE");
        toConfig.getStringInput("toJobConfig.outputDirectory").setValue("tmp");
        // set the driver config values
        MDriverConfig driverConfig = mysql2HdfsJob.getDriverConfig();
        driverConfig.getIntegerInput("throttlingConfig.numExtractors").setValue(3);

        Status status = client.saveJob(mysql2HdfsJob);
        if (status.canProceed()) {
            logger.info("Created Job with Job Id: {}", mysql2HdfsJob.getPersistenceId());
        } else {
            logger.info("Something went wrong creating the job");
        }
        assertTrue(mysql2HdfsJob.hasPersistenceId());

        //EE: start Sqoop Job
        MSubmission submission = client.startJob(mysql2HdfsJob.getPersistenceId(), new DefaultSubmissionCallback(), 1000);
        assertTrue(submission.getStatus().equals(SubmissionStatus.SUCCEEDED));
    }

    @Test
    public void testMySqlToKafka() throws InterruptedException {
        //EE: create Sqoop job
        mysql2KafkaJob = client.createJob(mysqlFromLink.getPersistenceId(), kafkaToLink.getPersistenceId());
        mysql2KafkaJob.setName("Job-Mysql2Kafka-" + System.currentTimeMillis());
        mysql2KafkaJob.setCreationUser(properties.getProperty("hadoop.user"));
        MFromConfig fromConfig = mysql2KafkaJob.getFromJobConfig();
        MToConfig toJobConfig = mysql2KafkaJob.getToJobConfig();

        //mysql from link config
        fromConfig.getStringInput("fromJobConfig.schemaName").setValue(properties.getProperty("sqoop.mysql.jdbc.schema"));
        fromConfig.getStringInput("fromJobConfig.tableName").setValue(properties.getProperty("sqoop.mysql.jdbc.table"));
        fromConfig.getStringInput("fromJobConfig.partitionColumn").setValue("emp_no");

        toJobConfig.getStringInput("toJobConfig.topic").setValue("sqoop-mysql2kafka-topic");
        // set the driver config values
        MDriverConfig driverConfig = mysql2KafkaJob.getDriverConfig();
        driverConfig.getIntegerInput("throttlingConfig.numExtractors").setValue(3);

        Status status = client.saveJob(mysql2KafkaJob);
        if (status.canProceed()) {
            logger.info("Created Job with Job Id: {}", mysql2KafkaJob.getPersistenceId());
        } else {
            logger.info("Something went wrong creating the job");
        }
        assertTrue(mysql2KafkaJob.hasPersistenceId());

        //EE: start Sqoop Job
        MSubmission submission = client.startJob(mysql2KafkaJob.getPersistenceId(), new DefaultSubmissionCallback(), 1000);
        assertTrue(submission.getStatus().equals(SubmissionStatus.SUCCEEDED));
    }


    @Test
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

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        //delete link and job
        if (mysql2HdfsJob != null && mysql2HdfsJob.hasPersistenceId()) {
            client.deleteJob(mysql2HdfsJob.getPersistenceId());
            logger.info("Sqoop job [{}, {}] is deleted..", mysql2HdfsJob.getName(), mysql2HdfsJob.getPersistenceId());
        }
        if (mysql2KafkaJob != null && mysql2KafkaJob.hasPersistenceId()) {
            client.deleteJob(mysql2KafkaJob.getPersistenceId());
            logger.info("Sqoop job [{}, {}] is deleted..", mysql2KafkaJob.getName(), mysql2KafkaJob.getPersistenceId());
        }
        if (mysqlFromLink != null && mysqlFromLink.hasPersistenceId()) {
            client.deleteLink(mysqlFromLink.getPersistenceId());
            System.out.printf("Sqoop link [%s, %s] is deleted..\n", mysqlFromLink.getName(), mysqlFromLink.getPersistenceId());
        }
        if (hdfsToLink != null && hdfsToLink.hasPersistenceId()) {
            client.deleteLink(hdfsToLink.getPersistenceId());
            System.out.printf("Sqoop link [%s, %s] is deleted..\n", hdfsToLink.getName(), hdfsToLink.getPersistenceId());
        }
        if (kafkaToLink != null && kafkaToLink.hasPersistenceId()) {
            client.deleteLink(kafkaToLink.getPersistenceId());
            System.out.printf("Sqoop link [%s, %s] is deleted..\n", kafkaToLink.getName(), kafkaToLink.getPersistenceId());
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
