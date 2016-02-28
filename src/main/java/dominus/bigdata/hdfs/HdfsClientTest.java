package dominus.bigdata.hdfs;


import dominus.junit.DominusBaseTestCase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * Hadoop’s org.apache.hadoop.fs.FileSystem is generic class to access and manage HDFS files/directories located in distributed environment.
 * EE: HDFS Configuration Files(core-site.xml and hdfs-site.xml) from Cloudera Manager
 */
public class HdfsClientTest extends DominusBaseTestCase {

    FileSystem fs;
    Path TEMP_DIR_PATH = new Path("/user/shawguo/tmp4test");
    String TEMP_DIR_STR = "/user/shawguo/tmp4test/";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Configuration conf = new Configuration();
        conf.addResource("hdfs-clientconfig-cdh/core-site.xml");
        conf.addResource("hdfs-clientconfig-cdh/hdfs-site.xml");
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
}
