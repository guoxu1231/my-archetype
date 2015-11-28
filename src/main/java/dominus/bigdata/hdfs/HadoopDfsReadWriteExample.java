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

            Path inFile = new Path("/user/hive/warehouse/src_customer_aa/part-00001");
            Path outFile = new Path("/user/shawguo/data/part-00001-copy");

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
