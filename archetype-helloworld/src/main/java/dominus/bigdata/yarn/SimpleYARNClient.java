package dominus.bigdata.yarn;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

/**
 * Simple YARN application to run n copies of a unix command - deliberately kept simple (with minimal error handling etc.)
 * hortonworks/simple-yarn-app
 * https://github.com/hortonworks/simple-yarn-app
 * <p/>
 * Usage:
 * Unmanaged mode
 * $ bin/hadoop jar $HADOOP_YARN_HOME/share/hadoop/yarn/hadoop-yarn-applications-unmanaged-am-launcher-2.1.1-SNAPSHOT.jar Client -classpath simple-yarn-app-1.0-SNAPSHOT.jar -cmd "java com.hortonworks.simpleyarnapp.ApplicationMaster /bin/date 2"
 *
 *
 * [Managed mode]
 * cd /opt/Development/github_repo/archetype-helloworld/target/classes
 * jar cf simple-yarn-app-1.0-SNAPSHOT.jar dominus/bigdata/yarn/*.class
 * $ bin/hadoop fs -copyFromLocal simple-yarn-app-1.0-SNAPSHOT.jar /user/shawguo/simple-yarn-app-1.0-SNAPSHOT.jar
 * $ bin/hadoop jar simple-yarn-app-1.0-SNAPSHOT.jar dominus.bigdata.yarn.SimpleYARNClient /bin/date 2 hdfs://user/shawguo/simple-yarn-app-1.0-SNAPSHOT.jar
 * $ bin/hadoop jar simple-yarn-app-1.0-SNAPSHOT.jar dominus.bigdata.yarn.SimpleYARNClient "/bin/date > /tmp/shawguo" 4 hdfs://scaj31cdh-ns/user/shawguo/simple-yarn-app-1.0-SNAPSHOT.jar
 * [Program Arguments]
 * /bin/date 4 hdfs:///user/shawguo/simple-yarn-app-1.0-SNAPSHOT.jar
 *
 * [Debug]
 * export HADOOP_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=4000
 * @author AmolFasale
 */
public class SimpleYARNClient {

    Configuration conf = new YarnConfiguration();

    public void run(String[] args) throws Exception {
        final String command = args[0];
        final int n = Integer.valueOf(args[1]);
        final Path jarPath = new Path(args[2]);

        /**
         * Create yarnClient
         */
        YarnConfiguration conf = new YarnConfiguration();
//        conf.addResource("yarn-clientconfig-cdh/core-site.xml");
//        conf.addResource("yarn-clientconfig-cdh/hdfs-site.xml");
//        conf.addResource("yarn-clientconfig-cdh/yarn-site.xml");
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(conf);
        yarnClient.start();

        /**
         * Create application via yarnClient
         */
        YarnClientApplication app = yarnClient.createApplication();

        /**
         * Set up the container launch context for the application master
         */
        ContainerLaunchContext amContainer = Records
                .newRecord(ContainerLaunchContext.class);
        amContainer.setCommands(Collections.singletonList("$JAVA_HOME/bin/java"
                + " -Xmx256M"
                + " dominus.bigdata.yarn.SimpleYARNApplicationClient" + " "
                + command + " " + String.valueOf(n) + " 1>"
                + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
                + " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
                + "/stderr"));

        /**
         * Setup jar for ApplicationMaster
         */
        LocalResource appMasterJar = Records.newRecord(LocalResource.class);
        setupAppMasterJar(jarPath, appMasterJar);
        amContainer.setLocalResources(Collections.singletonMap("simpleapp.jar",
                appMasterJar));

        /**
         * Setup CLASSPATH for ApplicationMaster
         */
        Map<String, String> appMasterEnv = new HashMap<String, String>();
        setupAppMasterEnv(appMasterEnv);
        amContainer.setEnvironment(appMasterEnv);

        /**
         * Set up resource type requirements for ApplicationMaster
         */
        Resource capability = Records.newRecord(Resource.class);
        capability.setMemory(256);
        capability.setVirtualCores(1);

        /**
         * Finally, set-up ApplicationSubmissionContext for the application
         */
        ApplicationSubmissionContext appContext = app
                .getApplicationSubmissionContext();
        appContext.setApplicationName("simple-yarn-app"); // application name
        appContext.setAMContainerSpec(amContainer);
        appContext.setResource(capability);
        appContext.setQueue("default"); // queue

        /**
         * Submit application
         */
        ApplicationId appId = appContext.getApplicationId();
        System.out.println("Submitting application " + appId);
        yarnClient.submitApplication(appContext);

        ApplicationReport appReport = yarnClient.getApplicationReport(appId);
        YarnApplicationState appState = appReport.getYarnApplicationState();
        while (appState != YarnApplicationState.FINISHED
                && appState != YarnApplicationState.KILLED
                && appState != YarnApplicationState.FAILED) {
            Thread.sleep(100);
            appReport = yarnClient.getApplicationReport(appId);
            appState = appReport.getYarnApplicationState();
        }

        System.out.println("Application " + appId + " finished with"
                + " state " + appState + " at " + appReport.getFinishTime());

    }

    /**
     * Function to setup application master jar
     *
     * @param jarPath
     * @param appMasterJar
     * @throws IOException
     */
    private void setupAppMasterJar(Path jarPath, LocalResource appMasterJar)
            throws IOException {
        FileStatus jarStat = FileSystem.get(conf).getFileStatus(jarPath);
        appMasterJar.setResource(ConverterUtils.getYarnUrlFromPath(jarPath));
        appMasterJar.setSize(jarStat.getLen());
        appMasterJar.setTimestamp(jarStat.getModificationTime());
        appMasterJar.setType(LocalResourceType.FILE);
        appMasterJar.setVisibility(LocalResourceVisibility.PUBLIC);
    }

    /**
     * Function to setup application environment
     *
     * @param appMasterEnv
     */
    @SuppressWarnings("deprecation")
    private void setupAppMasterEnv(Map<String, String> appMasterEnv) {
        for (String c : conf.getStrings(
                YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
            Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
                    c.trim());
        }
        Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
                Environment.PWD.$() + File.separator + "*");
    }

    public static void main(String[] args) throws Exception {
        SimpleYARNClient client = new SimpleYARNClient();
        client.run(args);
    }
}