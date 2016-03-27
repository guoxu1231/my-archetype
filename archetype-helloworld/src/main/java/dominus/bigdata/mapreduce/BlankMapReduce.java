package dominus.bigdata.mapreduce;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * What happens when you run MapReduce without setting a mapper or a reducer?
 */
public class BlankMapReduce {


    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.addResource("hdfs-clientconfig-cdh/core-site.xml");
        conf.addResource("hdfs-clientconfig-cdh/hdfs-site.xml");
        conf.addResource("yarn-clientconfig-cdh/mapred-site.xml");
        conf.addResource("yarn-clientconfig-cdh/yarn-site.xml");

        Job job = Job.getInstance(conf, "BlankMapReduce");
        /**
         * cd /opt/Development/github_repo/archetype-helloworld/target/classes
         * jar cf blank.jar dominus/bigdata/mapreduce/BlankMapReduce*.class
         */
        job.setJar("target/classes/blank.jar");

        job.setInputFormatClass(TextInputFormat.class);

        job.setMapperClass(Mapper.class);
        job.setReducerClass(Reducer.class);

        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setPartitionerClass(HashPartitioner.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        String outputSuffix = new SimpleDateFormat("yyyy_MM_dd_mm").format(new Date());
        FileInputFormat.addInputPath(job, new Path("/user/shawguo/data/SalesLog/sales1.dat"));
        FileOutputFormat.setOutputPath(job, new Path("/user/shawguo/data/SalesOutput_" +
                outputSuffix));
        System.out.println("OutputSuffix: " + outputSuffix);
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }


}
