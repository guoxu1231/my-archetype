package dominus.bigdata.hdfs;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Hadoop’s org.apache.hadoop.fs.FileSystem is generic class to access and manage HDFS files/directories located in distributed environment.
 * EE: HDFS Configuration Files(core-site.xml and hdfs-site.xml) from Cloudera Manager
 */
public class HdfsClientTest extends DominusJUnit4TestBase {

    FileSystem fs;
    String tempDir;
    Path tempDirPath;
    Configuration conf;

    @Override
    protected void doSetUp() throws Exception {

        tempDir = properties.getProperty("hadoop.hdfs.tmp");
        tempDirPath = new Path(tempDir);

        conf = new Configuration();
        conf.addResource(String.format("cdh-clientconfig/%s/hdfs/core-site.xml", activeProfile()));
        conf.addResource(String.format("cdh-clientconfig/%s/hdfs/hdfs-site.xml", activeProfile()));
        //disable file system cache
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);
        //kerberos config
        if ("kerberos".equals(properties.getProperty("hadoop.security.authentication"))) {
            conf.setBoolean("hadoop.security.authorization", true);
            conf.setStrings("hadoop.security.authentication", "kerberos");
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(properties.getProperty("hadoop.user"), properties.getProperty("hadoop.kerberos.keytab"));
        }
        fs = FileSystem.get(conf);
        logger.info("File System Capacity:{}G", fs.getStatus().getCapacity() / GB);

        assertEquals(fs.getScheme(), "hdfs");
        if (fs.mkdirs(tempDirPath))
            logger.info("create temp dir {}", tempDir);
    }

    @Override
    protected void doTearDown() throws Exception {
        if (fs.delete(tempDirPath, true))
            logger.info(tempDir + " is deleted.");
        if (fs != null) fs.close();
    }

    @Test
    public void testCopyFromLocalFile() throws IOException {

        assertTrue(fs.exists(tempDirPath));
        //copy single file, overwrite
        fs.copyFromLocalFile(false, true, new Path(resourceLoader.getResource("classpath:log4j.properties").getURI()), tempDirPath);
        assertTrue(fs.exists(new Path(tempDir + "/log4j.properties")));
        //copy folder
        fs.copyFromLocalFile(false, true, new Path(resourceLoader.getResource("classpath:oozie/apps/demo").getURI()), tempDirPath);
        assertTrue(fs.isDirectory(new Path(tempDir + "/demo")));
        assertTrue(fs.exists(new Path(tempDir + "/demo")));
    }

    /**
     * a good client always knows these two things: BLOCKSIZE and REPLICATION FACTOR
     * The block size and replication factor are configurable per file.
     *
     * @throws IOException
     */
    @Test
    public void testBlockAndReplication() throws IOException {

        conf.set("dfs.blocksize", "2m");
        conf.set("dfs.replication", "2");

        final File sampleFile = this.createSampleFile(1 * MB);
        fs.copyFromLocalFile(false, true, new Path(sampleFile.toURI()), tempDirPath);
        FileStatus status = fs.getFileStatus(new Path(tempDir, sampleFile.getName()));
        logger.info(status.toString());
        assertEquals(2, status.getReplication());
        assertEquals(2 * MB, status.getBlockSize());//TODO
    }
}
