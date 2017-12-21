package origin.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Hadoop’s org.apache.hadoop.fs.FileSystem is generic class to access and manage HDFS files/directories located in distributed environment.
 * EE: HDFS Configuration Files(core-site.xml and hdfs-site.xml) from Cloudera Manager
 * <p>
 * EE:kerberos config
 * 1, Using UserGroupInformation
 * 2, Proper krb5.conf is required to determine default realm and KDC.
 * a,If the system property java.security.krb5.conf is set, its value is assumed to specify the path and file name.
 * b,If that system property value is not set, then the configuration file is looked for in the directory:<java-home>/lib/security
 * c,If the file is still not found, then an attempt is made to locate it as follows:/etc/krb5.conf
 * d,If the file is still not found, and the configuration information being searched for is not the default realm and KDC, then implementation-specific defaults are used.
 */
public class HdfsClientTest extends DominusJUnit4TestBase {

    FileSystem fs;
    String tempDir;
    Path tempDirPath;
    Configuration conf;

    @Override
    protected void doSetUp() throws Exception {

        conf = new Configuration();
        tempDir = properties.getProperty("hadoop.hdfs.tmp");
        tempDirPath = new Path(tempDir);

        //EE: name node connection info
        conf.addResource(String.format("spring-container/props/%s/hdfs/core-site.xml", activeProfile()));
        conf.addResource(String.format("spring-container/props/%s/hdfs/hdfs-site.xml", activeProfile()));
        //EE:kerberos config
        System.setProperty("java.security.krb5.conf", properties.getProperty("hadoop.krb5.conf"));
        if ("kerberos".equals(properties.getProperty("hadoop.security.authentication"))) {
            conf.setBoolean("hadoop.security.authorization", true);
            conf.setStrings("hadoop.security.authentication", "kerberos");
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(properties.getProperty("hadoop.user"), properties.getProperty("hadoop.kerberos.keytab"));
        }
        //disable file system cache
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);

        fs = FileSystem.get(conf);

        logger.info("File System Capacity:{}G", fs.getStatus().getCapacity() / GB);

//        assertEquals(fs.getScheme(), "hdfs");
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

        //overridden dfs parameters
        conf.set("dfs.blocksize", "2m");
        conf.set("dfs.replication", "3");

        //overridden DFS client
        FileSystem overriddenFS = FileSystem.get(conf);
        final File sampleFile = this.createSampleFile(1 * MB);
        overriddenFS.copyFromLocalFile(false, true, new Path(sampleFile.toURI()), tempDirPath);
        overriddenFS.close();

        Path testDfsFile = new Path(tempDir, sampleFile.getName());
        FileStatus status = fs.getFileStatus(testDfsFile);
        logger.info(status.toString());
        assertEquals(3, status.getReplication());
//        assertEquals(2 * MB, status.getBlockSize());  TODO might not honor

        //get file block location info
        BlockLocation[] locations = fs.getFileBlockLocations(testDfsFile, 0, status.getLen());
        logger.info("FileBlockLocation: {}", locations.length);
        for (BlockLocation location : locations)
            logger.info("hosts:{} offset:{} length:{}", Arrays.toString(location.getNames()), location.getOffset(), location.getLength());

        //check md5sum
        FileChecksum md51 = fs.getFileChecksum(testDfsFile);
        logger.info(md51.toString());
    }

    @Test
    public void testDFSCopyWrite() throws IOException {
        final File sampleFile = this.createSampleFile(2 * MB);
        fs.copyFromLocalFile(false, true, new Path(sampleFile.toURI()), tempDirPath);
        Path testDfsFile = new Path(tempDir, sampleFile.getName());

        //test DFS copy-write
        FSDataInputStream in = fs.open(testDfsFile);
        FSDataOutputStream out = fs.create(new Path(testDfsFile.getParent(), "test-copy-write"));
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        int bytesRead;
        byte[] bytes;
        while ((bytesRead = in.read(buffer)) > 0) {
            bytes = buffer.array();
            out.write(bytes, 0, bytesRead);
            buffer.clear();
        }
        in.close();
        out.close();
        assertEquals(fs.getFileChecksum(testDfsFile), fs.getFileChecksum(new Path(testDfsFile.getParent(), "test-copy-write")));
    }
}
