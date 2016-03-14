package dominus.bigdata.hdfs;


import dominus.framework.junit.DominusBaseTestCase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;

/**
 * Hadoop’s org.apache.hadoop.fs.FileSystem is generic class to access and manage HDFS files/directories located in distributed environment.
 * EE: HDFS Configuration Files(core-site.xml and hdfs-site.xml) from Cloudera Manager
 */
public class HdfsClientTest extends DominusBaseTestCase {

    FileSystem fs;
    Path TEMP_DIR_PATH = new Path("/user/shawguo/tmp4test");
    String TEMP_DIR_STR = "/user/shawguo/tmp4test/";
    Configuration conf;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        conf = new Configuration();
        conf.addResource("hdfs-clientconfig-cdh/core-site.xml");
        conf.addResource("hdfs-clientconfig-cdh/hdfs-site.xml");
        //EE: disable file system cache
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);
        try {
            fs = FileSystem.get(conf);
            out.printf("File System Capacity:%sG\n", fs.getStatus().getCapacity() / GB);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(fs);
        assertEquals(fs.getScheme(), "hdfs");
        fs.mkdirs(TEMP_DIR_PATH);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fs.delete(TEMP_DIR_PATH, true);
        if (fs != null) fs.close();
    }

    public void testCopyFromLocalFile() throws IOException {

        assertTrue(fs.exists(TEMP_DIR_PATH));
        //copy single file, overwrite
        fs.copyFromLocalFile(false, true, new Path(resourceLoader.getResource("classpath:log4j.properties").getURI()), TEMP_DIR_PATH);
        assertTrue(fs.exists(new Path(TEMP_DIR_STR + "log4j.properties")));
        //copy folder
        fs.copyFromLocalFile(false, true, new Path(resourceLoader.getResource("classpath:oozie/apps/demo").getURI()), TEMP_DIR_PATH);
        assertTrue(fs.isDirectory(new Path(TEMP_DIR_STR + "demo")));
        assertTrue(fs.exists(new Path(TEMP_DIR_STR + "demo")));
    }

    /**
     * a good client always knows these two things: BLOCKSIZE and REPLICATION FACTOR
     *
     * @throws IOException
     */
    public void testBlockAndReplication() throws IOException {
        /**
         * The default block size for new files, in bytes.
         * You can use the following suffix (case insensitive): k(kilo), m(mega), g(giga), t(tera), p(peta), e(exa) to specify the size (such as 128k, 512m, 1g, etc.),
         * Or provide complete size in bytes (such as 134217728 for 128 MB).
         */
        conf.set("dfs.blocksize", "2m");
        //EE:org.apache.hadoop.ipc.RemoteException(java.io.IOException): Specified block size is less than configured minimum value (dfs.namenode.fs-limits.min-block-size): 20480 < 1048576
        /**
         * Default block replication. The actual number of replications can be specified when the file is created.
         * The default is used if replication is not specified in create time.
         */
        conf.set("dfs.replication", "1");
        //TODO replication factor large than cluster size
        //EE:org.apache.hadoop.ipc.RemoteException(java.io.IOException): Requested replication factor of 1000 exceeds maximum of 512

        FileSystem test_fs = null;
        test_fs = FileSystem.get(conf);

        final File sampleFile = this.createSampleFile(1 * MB);
        test_fs.copyFromLocalFile(false, true, new Path(sampleFile.toURI()), TEMP_DIR_PATH);
        FileStatus status = test_fs.getFileStatus(new Path(TEMP_DIR_PATH, sampleFile.getName()));
        assertEquals(1, status.getReplication());
        assertEquals(2 * MB, status.getBlockSize());
    }
}
