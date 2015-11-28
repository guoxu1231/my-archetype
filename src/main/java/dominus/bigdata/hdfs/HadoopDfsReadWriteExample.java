package dominus.bigdata.hdfs;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Simple Example to Read and Write files from Hadoop DFS
 * https://wiki.apache.org/hadoop/HadoopDfsReadWriteExample
 */
public class HadoopDfsReadWriteExample {

    public static void main(String[] args) {


        Configuration conf = new Configuration();
        conf.addResource("hdfs-clientconfig-cdh/core-site.xml");
        conf.addResource("hdfs-clientconfig-cdh/hdfs-site.xml");

        try {
            FileSystem fs = FileSystem.get(conf);
            System.out.println("File System Capacity:" + fs.getStatus().getCapacity() / (1024 * 1024 * 1024) + "G");
//            copyWrite(fs, "/user/hive/warehouse/src_customer_aa/part-00001", "/user/shawguo/data/part-00001-copy");
            fileBlockLocation(fs, "/user/shawguo/data/part-00001-copy");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * File System Capacity:171938G
     * /user/custbug/sales1.dat
     * BlockSize: 1048576 File Size: 2164M DefaultBlockSize: 1M
     * FileBlockLocation: 2165
     * 0,1048576,scaj31bda06.website.com,scaj31bda02.website.com,scaj31bda03.website.com
     * 1048576,1048576,scaj31bda06.website.com,scaj31bda01.website.com,scaj31bda03.website.com
     * 2097152,1048576,scaj31bda02.website.com,scaj31bda04.website.com,scaj31bda01.website.com
     * 3145728,1048576,scaj31bda06.website.com,scaj31bda01.website.com,scaj31bda03.website.com
     * 4194304,1048576,scaj31bda06.website.com,scaj31bda02.website.com,scaj31bda03.website.com
     * 5242880,1048576,scaj31bda03.website.com,scaj31bda01.website.com,scaj31bda02.website.com
     * 6291456,1048576,scaj31bda02.website.com,scaj31bda01.website.com,scaj31bda03.website.com
     * <p/>
     * <p/>
     * <B>Client can specifify block size and replicatior factor!!</B><P>
     * File System Capacity:171938G
     * /user/shawguo/data/part-00001-copy
     * BlockSize: 524288000 File Size: 39M DefaultBlockSize: 500M
     * FileBlockLocation: 1
     * 0,41924400,scaj31bda05.website.com,scaj31bda03.website.com,scaj31bda02.website.com,scaj31bda04.website.com,scaj31bda06.website.com,scaj31bda01.website.com
     *
     * @param fs
     */
    public static void fileBlockLocation(FileSystem fs, String path) {

        try {
            Path inFile = new Path(path);
            FileStatus status = fs.getFileStatus(inFile);

            System.out.println(inFile);
            System.out.println("BlockSize: " + status.getBlockSize() + " File Size: " + status.getLen() / (1024 * 1024) + "M"
                    + " DefaultBlockSize: " + status.getBlockSize() / (1024 * 1024) + "M");

            BlockLocation[] locations = fs.getFileBlockLocations(inFile, 0, status.getLen());
            System.out.println("FileBlockLocation: " + locations.length);
            for (int i = 0; i < locations.length; i++)
                System.out.println(locations[i].toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void copyWrite(FileSystem fs, String inFilePath, String outFilePath) {
        Path inFile = new Path(inFilePath);
        Path outFile = new Path(outFilePath);

        try {
            if (!fs.exists(inFile))
                System.out.println("Input file not found");
            if (fs.exists(outFile))
                fs.delete(outFile, false);
            FSDataInputStream in = fs.open(inFile);
            FSDataOutputStream out = fs.create(outFile);

            ByteBuffer buffer = ByteBuffer.allocate(1000);
            int bytesRead;
            byte[] bytes;
            while ((bytesRead = in.read(buffer)) > 0) {
                bytes = buffer.array();
                System.out.print(new String(bytes, 0, bytesRead));
                out.write(bytes, 0, bytesRead);
                buffer.clear();
            }
            in.close();
            out.close();

            FileChecksum md51 = fs.getFileChecksum(inFile);
            FileChecksum md52 = fs.getFileChecksum(outFile);

            System.out.println(md51.hashCode());
            System.out.println(md52.hashCode());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
